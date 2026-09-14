package com.nhnacademy.ruleengine.engine.handler;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.AverageNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.DurationNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.GradientNodeConfig;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import com.nhnacademy.ruleengine.engine.model.FlowFailureEvent;
import com.nhnacademy.ruleengine.engine.publisher.FlowFailureEventPublisher;
import com.nhnacademy.ruleengine.engine.repository.SensorTimeSeriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
역할: 룰엔진이 플로우를 실행하기 전에 조건 노드들이 필요로 하는 센서 측정값을 redis 시꼐열 db에 사전 저장 및 사전 저장에 성공한 플로우만 선별하는 클래스
1. 장애 격리: 특정 플로우의 시계열 데이터 저장/준비가 실패하더라도 해당 플로우만 에러 이벤를 발행하고 정상 준비된 다른 플로우는 정상 실행되도록 격리
2. 중복 저장 및 에러 캐싱 방지: 한 요청 내에서 여러 플로우가 동일한 시계열 데이터를 저장하려 할 때, 중복 db 저장을 방지하고 이전에 발생한 에러를 재사용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimeSeriesPreparationService {

    private final SensorTimeSeriesRepository timeSeriesRepository;
    private final FlowFailureEventPublisher flowFailureEventPublisher;

    // 시계열 저장이 필요한 플로우는 개별적으로 준비하고, 준비 실패 플로우만 실패 이벤트로 분리한다.
    public List<ExecutableFlow> prepareAndFilterDispatchableFlows(
            EnvironmentContext environmentContext,
            List<ExecutableFlow> flows
    ) {
        List<ExecutableFlow> dispatchableFlows = new ArrayList<>();
        Set<TimeSeriesSaveKey> completedSaves = new HashSet<>();
        Map<TimeSeriesSaveKey, RuntimeException> failedSaves = new HashMap<>();

        for(ExecutableFlow flow : flows) {
            try {
                //하나의 ExecutableFlow 내부 노드들을 스캔하여 어떤 측정 항목(measurementType)을 저장해야하는지 수집
                TimeSeriesRequirements requirements = collectTimeSeriesRequirements(flow);

                //수집된 센서 페이로드(environmentContext.metrics())를 순회하며, 플로우가 요구하는 측정 항목만 골라 Redis 시계열 저장소에 저장
                recordRequiredTimeSeries(environmentContext, requirements, completedSaves, failedSaves);

                dispatchableFlows.add(flow);
            } catch (RuntimeException e) {
                log.error("플로우 시계열 준비 실패 flowId={}, roomId={}", flow.flowId(), flow.roomId(), e);
                flowFailureEventPublisher.publish(FlowFailureEvent.of(flow, environmentContext, e));
            }
        }

        return dispatchableFlows;
    }

    //수집된 센서 페이로드(environmentContext.metrics())를 순회하며, 플로우가 요구하는 측정 항목만 골라 Redis 시계열 저장소에 저장
    private void recordRequiredTimeSeries(
            EnvironmentContext environmentContext,
            TimeSeriesRequirements requirements,
            Set<TimeSeriesSaveKey> completedSaves,
            Map<TimeSeriesSaveKey, RuntimeException> failedSaves
    ) {
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
                                    TimeSeriesSaveKey key = TimeSeriesSaveKey.room(type, metricInfo.value(), environmentContext.updatedAt());
                                    saveTimeSeriesIfNecessary(
                                            key,
                                            completedSaves,
                                            failedSaves,
                                            () -> timeSeriesRepository.save(environmentContext.roomId(), type, metricInfo.value(), environmentContext.updatedAt())
                                    );
                                }

                                // Gradient 노드는 같은 장비의 변화량을 봐야 하므로 devEui별 시계열에 따로 저장한다.
                                if(requirements.deviceLevelTypes().contains(type)) {
                                    TimeSeriesSaveKey key = TimeSeriesSaveKey.device(type, metricInfo.devEui(), metricInfo.value(), metricInfo.updatedAt());
                                    saveTimeSeriesIfNecessary(
                                            key,
                                            completedSaves,
                                            failedSaves,
                                            () -> timeSeriesRepository.save(environmentContext.roomId(), type, metricInfo.devEui(), metricInfo.value(), metricInfo.updatedAt())
                                    );
                                }
                            },
                            () -> log.debug("지원하지 않는 metric 시계열 저장 스킵 roomId={}, metric={}", environmentContext.roomId(), metricInfo.metric())
                    );
        }
    }

    //실제 db 저장 연산을 실행하며, 중복 저장 방지 및 에러 캐싱을 담당
    private void saveTimeSeriesIfNecessary(
            TimeSeriesSaveKey key,
            Set<TimeSeriesSaveKey> completedSaves,
            Map<TimeSeriesSaveKey, RuntimeException> failedSaves,
            Runnable saveOperation
    ) {
        //이전 실패 확인, 동일한 TimeSeriesSaveKey로 db 실패 기록이 있다면 db에 재시도 하지않고 예외를 던짐
        RuntimeException previousFailure = failedSaves.get(key);
        if(previousFailure != null) {
            throw previousFailure;
        }
        // 이번 요청 내에서 저장 완료된 key라면 db 저장을 건너뜀
        if(completedSaves.contains(key)) {
            return;
        }

        try {
            saveOperation.run();//레시드 시계열 데이터 저장 수행
            completedSaves.add(key);//completedSaves에 기록
        } catch (RuntimeException e) {
            failedSaves.put(key, e);//실패시 failedSaves에 기록 후 예외
            throw e;//FlowFailureEvent 발행으로 이어짐
        }
    }

    //하나의 ExecutableFlow 내부 노드들을 스캔하여 어떤 측정 항목(measurementType)을 저장해야하는지 수집
    private TimeSeriesRequirements collectTimeSeriesRequirements(ExecutableFlow flow) {
        Set<MeasurementType> roomLevelTypes = EnumSet.noneOf(MeasurementType.class);
        Set<MeasurementType> deviceLevelTypes = EnumSet.noneOf(MeasurementType.class);

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

        return new TimeSeriesRequirements(roomLevelTypes, deviceLevelTypes);
    }

    // 특정 플로우가 필요로하는 방 단위/ 장비 단위 MeasurementType Set을 묶어 전달하는 DTO
    private record TimeSeriesRequirements(
            Set<MeasurementType> roomLevelTypes,
            Set<MeasurementType> deviceLevelTypes
    ) {
        private boolean isEmpty() {
            return roomLevelTypes.isEmpty() && deviceLevelTypes.isEmpty();
        }
    }

    //시계열 데이터 저장 범위를 구분
    private enum TimeSeriesScope {
        ROOM,
        DEVICE
    }

    //중복 저장 및 에러 캐싱을 위한 식별자(Key) 객체
    private record TimeSeriesSaveKey(
            TimeSeriesScope scope,
            MeasurementType type,
            String devEui,
            double value,
            Instant timestamp
    ) {
        private static TimeSeriesSaveKey room(MeasurementType type, double value, Instant timestamp) {
            return new TimeSeriesSaveKey(TimeSeriesScope.ROOM, type, null, value, timestamp);
        }

        private static TimeSeriesSaveKey device(MeasurementType type, String devEui, double value, Instant timestamp) {
            return new TimeSeriesSaveKey(TimeSeriesScope.DEVICE, type, devEui, value, timestamp);
        }
    }
}
