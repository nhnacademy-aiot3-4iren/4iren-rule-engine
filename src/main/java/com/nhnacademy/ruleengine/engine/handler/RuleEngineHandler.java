package com.nhnacademy.ruleengine.engine.handler;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.AverageNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.DurationNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.GradientNodeConfig;
import com.nhnacademy.ruleengine.engine.dispatcher.FlowDispatcher;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.flow.FlowLoader;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import com.nhnacademy.ruleengine.engine.repository.SensorTimeSeriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuleEngineHandler {
    private final FlowLoader flowLoader;
    private final FlowDispatcher dispatcher;
    private final SensorTimeSeriesRepository timeSeriesRepository;

    /**
     * 센서 페이로드 하나를 받아 해당 방의 활성 플로우를 로드하고 룰 엔진 실행을 시작한다.
     */
    public CompletableFuture<Void> process(EnvironmentContext environmentContext){
        Long roomId = environmentContext.roomId();

        log.info("룰 엔진 처리 시작 roomId={}, 측정값수={}", roomId, environmentContext.metrics().size());

        // 현재 방에 연결되어 있고 활성화된 플로우 정의들을 실행 가능한 형태로 불러온다.
        List<ExecutableFlow> flows = flowLoader.load(roomId);
        if(flows.isEmpty()){
            log.info("실행할 활성 플로우 없음 roomId={}", roomId);
            // 실제 비동기 작업을 만들지 않고, 호출자에게는 정상 완료된 Future를 돌려준다.
            return CompletableFuture.completedFuture(null);
        }
        log.info("실행 대상 플로우 로드 완료 roomId={}, flowCount={}", roomId, flows.size());
        // Average/Duration/Gradient 같은 시간 윈도우 조건이 나중에 조회할 수 있도록 현재 센서값을 먼저 저장한다.
        recordRequiredTimeSeries(environmentContext, flows);

        // 플로우가 존재하면 dispatcher에 비동기 실행을 맡기고, 방 단위 처리 결과만 여기서 로깅한다.
        return dispatcher.dispatch(flows, environmentContext).whenComplete((r, ex)->{
            if(ex != null){
                log.error("roomId={} 룰 엔진 플로우 실행 중 최종 실패 발생", roomId, ex);
                return;
            }
            log.info("룰 엔진 처리 완료 roomId={}, flowCount={}", roomId, flows.size());
        });
    }

    /**
     * 현재 방의 플로우들이 필요로 하는 측정 항목만 골라 Redis 시계열 저장소에 기록한다.
     */
    private void recordRequiredTimeSeries(EnvironmentContext environmentContext, List<ExecutableFlow> flows) {
        TimeSeriesRequirements requirements = collectTimeSeriesRequirements(flows);
        if(requirements.isEmpty()) {
            log.debug("시계열 조건 노드 없음. 시계열 저장 스킵 roomId={}", environmentContext.roomId());
            return;
        }

        for(EnvironmentContext.MetricInfo metricInfo : environmentContext.metrics()) {
            // 외부 페이로드의 metric 문자열을 내부 enum으로 변환해야 노드 설정과 같은 기준으로 비교할 수 있다.
            MeasurementType.findByExternalCode(metricInfo.metric())
                    .ifPresentOrElse(
                            type -> {
                                // Average/Duration 노드는 방 단위 시계열을 사용하므로 devEui 없이 저장한다.
                                if(requirements.roomLevelTypes().contains(type)) {
                                    timeSeriesRepository.save(environmentContext.roomId(), type, metricInfo.value(), environmentContext.updatedAt());
                                }

                                // Gradient 노드는 같은 장비의 변화량을 봐야 하므로 devEui별 시계열에 따로 저장한다.
                                if(requirements.deviceLevelTypes().contains(type)) {
                                    timeSeriesRepository.save(environmentContext.roomId(), type, metricInfo.devEui(), metricInfo.value(), metricInfo.updatedAt());
                                }
                            },
                            () -> log.debug("지원하지 않는 metric 시계열 저장 스킵 roomId={}, metric={}", environmentContext.roomId(), metricInfo.metric())
                    );
        }
    }

    /**
     * 전체 플로우를 훑어서 어떤 측정 항목을 시계열로 저장해야 하는지 계산한다.
     */
    private TimeSeriesRequirements collectTimeSeriesRequirements(List<ExecutableFlow> flows) {
        Set<MeasurementType> roomLevelTypes = EnumSet.noneOf(MeasurementType.class);
        Set<MeasurementType> deviceLevelTypes = EnumSet.noneOf(MeasurementType.class);

        for(ExecutableFlow flow : flows) {
            for(ExecutableFlow.ExecutableNode node : flow.nodeMap().values()) {
                NodeConfig config = node.nodeConfig();
                // 노드 타입별로 시간 윈도우를 쓰는 측정 항목만 뽑아 시계열 저장 대상에 반영한다.
                switch(node.nodeType()) {
                    case AVERAGE -> {
                        AverageNodeConfig averageConfig = (AverageNodeConfig) config;
                        roomLevelTypes.add(averageConfig.measurementType());
                    }
                    case DURATION -> {
                        DurationNodeConfig durationConfig = (DurationNodeConfig) config;
                        roomLevelTypes.add(durationConfig.measurementType());
                    }
                    case GRADIENT -> {
                        GradientNodeConfig gradientConfig = (GradientNodeConfig) config;
                        deviceLevelTypes.add(gradientConfig.measurementType());
                    }
                    default -> {
                    }
                }
            }
        }

        return new TimeSeriesRequirements(roomLevelTypes, deviceLevelTypes);
    }

    /**
     * 방 단위 시계열과 장비 단위 시계열의 저장 대상 측정 항목을 함께 담는 내부 전용 값 객체다.
     */
    private record TimeSeriesRequirements(
            Set<MeasurementType> roomLevelTypes,
            Set<MeasurementType> deviceLevelTypes
    ) {
        private boolean isEmpty() {
            return roomLevelTypes.isEmpty() && deviceLevelTypes.isEmpty();
        }
    }
}
