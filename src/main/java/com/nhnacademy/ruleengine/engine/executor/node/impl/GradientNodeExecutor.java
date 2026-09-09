package com.nhnacademy.ruleengine.engine.executor.node.impl;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.GradientNodeConfig;
import com.nhnacademy.ruleengine.engine.executor.ExecutionPath;
import com.nhnacademy.ruleengine.engine.executor.FlowContext;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutionResult;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutor;
import com.nhnacademy.ruleengine.engine.executor.node.OperatorEvaluator;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.FlowRuntime;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.model.AlertEvent;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import com.nhnacademy.ruleengine.engine.repository.SensorTimeSeriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GradientNodeExecutor implements NodeExecutor {

    private final SensorTimeSeriesRepository repository;

    @Override
    public NodeType supportNodeType() {
        return NodeType.GRADIENT;
    }

    /**
     * 기울기 노드를 실행한다.
     * 현재 페이로드의 최신 측정값을 기준 시각으로 삼고, 같은 장비(devEui)의 과거 값과 비교해 초당 변화량을 계산한다.
     */
    @Override
    public NodeExecutionResult execute(ExecutableFlow.ExecutableNode node, FlowContext context, ExecutionPath path, FlowRuntime runtime) {
        GradientNodeConfig config = (GradientNodeConfig) node.nodeConfig();
        Long roomId = context.roomId();

        // 현재 들어온 센서 페이로드에서 이 기울기 노드가 검사할 측정 항목을 찾는다.
        EnvironmentContext.MetricInfo metricInfo = findMetric(context.environmentContext(), config.measurementType());
        if(metricInfo == null) {
            log.debug("node({}) - roomId({})에 {} 최신값 없음. 기울기 조건 미충족 처리", node.nodeId(), roomId, config.measurementType());
            return NodeExecutionResult.of(false, path.append(buildNodeResult(node, config, null)));
        }

        // 기울기 계산의 끝 시각은 서버 처리 시각이 아니라 실제 센서 값이 갱신된 시각이어야 한다.
        Instant to = metricInfo.updatedAt();
        // 설정된 windowSec만큼 과거로 이동해서 조회 시작 시각을 만든다.
        Instant from = to.minusSeconds(config.windowSec());

        // 기울기는 방 전체 평균이 아니라 같은 devEui 장비의 시계열만 사용해서 계산한다.
        List<SensorTimeSeriesRepository.TimeSeriesPoint> points = repository.getRange(roomId, config.measurementType(), metricInfo.devEui(), from, to);

        Double gradient = calculateGradient(points);

        // 계산된 기울기가 없으면 조건은 실패로 보고, 있으면 노드 설정의 비교 연산자로 판단한다.
        boolean passed = gradient != null && OperatorEvaluator.evaluate(config.operator(), gradient, config.gradient());
        if(gradient == null) {
            log.debug("node({}) - roomId({})에 {} 윈도우({}s) 내 기울기 계산 불가. 조건 미충족 처리", node.nodeId(), roomId, config.measurementType(), config.windowSec());
        }

        return NodeExecutionResult.of(passed, path.append(buildNodeResult(node, config, gradient)));
    }

    /**
     * 현재 페이로드에 포함된 측정값 중, 특정 하는 MeasurementType의 최신값을 찾는다.
     */
    private EnvironmentContext.MetricInfo findMetric(EnvironmentContext environmentContext, MeasurementType measurementType) {
        if(environmentContext == null || environmentContext.metrics() == null) {
            return null;
        }
        // 외부 페이로드의 metric 문자열과 내부 MeasurementType 이름을 대소문자 무시 방식으로 맞춘다.
        return environmentContext.metrics().stream()
                .filter(metricInfo -> measurementType.name().equalsIgnoreCase(metricInfo.metric()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 알림 이벤트에 남길 노드 실행 결과 객체를 만든다.
     */
    private AlertEvent.NodeResult buildNodeResult(ExecutableFlow.ExecutableNode node, GradientNodeConfig config, Double gradient) {
        return new AlertEvent.NodeResult(
                node.nodeType().name(),
                config.measurementType().name(),
                config.operator().getSymbol(),
                " 기울기",
                config.gradient(),
                truncateToSecondDecimalPlace(gradient)
        );
    }

    private Double truncateToSecondDecimalPlace(Double value) {
        if(value == null) {
            return null;
        }
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.DOWN)
                .doubleValue();
    }

    /**
     * 조회된 시계열의 가장 오래된 값과 가장 최신 값만 사용해 초당 변화량을 계산한다.
     */
    private Double calculateGradient(List<SensorTimeSeriesRepository.TimeSeriesPoint> points) {
        if(points.size() < 2) {
            return null;
        }

        // getRange에서 시간순으로 정렬해주므로 첫 번째 값은 윈도우 내 가장 오래된 값이다.
        SensorTimeSeriesRepository.TimeSeriesPoint oldest = points.getFirst();
        // 마지막 값은 윈도우 내 가장 최신 값이다.
        SensorTimeSeriesRepository.TimeSeriesPoint latest = points.getLast();

        // 밀리초 차이를 초 단위 double로 바꿔, 1초 미만 간격도 기울기 계산에 반영한다.
        double elapsedSeconds = Duration.between(oldest.timestamp(), latest.timestamp()).toMillis() / 1000.0;
        if(elapsedSeconds <= 0) {
            return null;
        }

        // 기울기 = 값 변화량 / 시간 변화량. 결과 단위는 "측정값 단위 per second"다.
        return (latest.value() - oldest.value()) / elapsedSeconds;
    }
}
