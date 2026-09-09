package com.nhnacademy.ruleengine.engine.executor.node.impl;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.DurationNodeConfig;
import com.nhnacademy.ruleengine.engine.executor.ExecutionPath;
import com.nhnacademy.ruleengine.engine.executor.FlowContext;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutionResult;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutor;
import com.nhnacademy.ruleengine.engine.executor.node.OperatorEvaluator;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.FlowRuntime;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.model.AlertEvent;
import com.nhnacademy.ruleengine.engine.repository.SensorTimeSeriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DurationNodeExecutor implements NodeExecutor {

    private final SensorTimeSeriesRepository repository;

    @Override
    public NodeType supportNodeType() {
        return NodeType.DURATION;
    }

    @Override
    public NodeExecutionResult execute(ExecutableFlow.ExecutableNode node, FlowContext context, ExecutionPath path, FlowRuntime runtime) {
        DurationNodeConfig config = (DurationNodeConfig) node.nodeConfig();
        Long roomId = context.roomId();

        Instant to = context.roomStateUpdatedAt();
        Instant from = to.minusSeconds(config.durationSec());

        List<SensorTimeSeriesRepository.TimeSeriesPoint> points = repository.getRange(roomId, config.measurementType(), from, to);
        SensorTimeSeriesRepository.TimeSeriesPoint baseline = repository.getLatestBeforeOrAt(roomId, config.measurementType(), from);

        boolean passed = isDurationSatisfied(baseline, points, config);
        Double lastValue = points.isEmpty() ? null : points.getLast().value();

        if(points.isEmpty()) {
            log.debug("node({}) - roomId({})에 {} 윈도우({}s) 내 데이터 없음. 조건 미충족 처리", node.nodeId(), roomId, config.measurementType(), config.durationSec());
        }

        return NodeExecutionResult.of(passed, path.append(buildNodeResult(node, config, lastValue)));
    }

    private AlertEvent.NodeResult buildNodeResult(ExecutableFlow.ExecutableNode node, DurationNodeConfig config, Double lastValue) {
        return new AlertEvent.NodeResult(
                node.nodeType().name(),
                config.measurementType().name(),
                config.operator().getSymbol(),
                config.unit(),
                config.threshold(),
                lastValue
        );
    }

    //지정한 시간(Duration) 동안 특정 조건이 단 한 순간도 끊이지 않고 지속해서 만족되었는가?"를 검증
    private boolean isDurationSatisfied(
            SensorTimeSeriesRepository.TimeSeriesPoint baseline,
            List<SensorTimeSeriesRepository.TimeSeriesPoint> points,
            DurationNodeConfig config
    ) {
        if(baseline == null) {
            return false;
        }

        if(!OperatorEvaluator.evaluate(config.operator(), baseline.value(), config.threshold())) {
            return false;
        }

        return points.stream()
                .allMatch(point -> OperatorEvaluator.evaluate(config.operator(), point.value(), config.threshold()));
    }
}
