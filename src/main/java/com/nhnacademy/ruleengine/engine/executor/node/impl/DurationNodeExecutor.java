package com.nhnacademy.ruleengine.engine.executor.node.impl;

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

        Instant to = context.triggeredAt();
        Instant from = to.minusSeconds(config.durationSec());

        List<SensorTimeSeriesRepository.TimeSeriesPoint> points = repository.getRange(roomId, config.measurementType(), from, to);

        boolean passed = isDurationSatisfied(points, from, config);
        Double lastValue = points.isEmpty() ? null : points.getLast().value();

        if(points.isEmpty()) {
            log.debug("node({}) - roomId({})에 {} 윈도우({}s) 내 데이터 없음. 조건 미충족 처리", node.nodeId(), roomId, config.measurementType(), config.durationSec());
        }

        AlertEvent.NodeResult nodeResult= new AlertEvent.NodeResult(
                node.nodeType().name(),
                config.measurementType().name(),
                config.operator().getSymbol(),
                config.unit(),
                config.threshold(),
                lastValue
        );

        return NodeExecutionResult.of(passed, path.append(nodeResult));
    }

    private boolean isDurationSatisfied(List<SensorTimeSeriesRepository.TimeSeriesPoint> points, Instant from, DurationNodeConfig config) {
        if(points.isEmpty()) {
            return false;
        }

        SensorTimeSeriesRepository.TimeSeriesPoint oldest = points.getFirst();
        if(oldest.timestamp().isAfter(from)) {
            return false;
        }

        return points.stream()
                .allMatch(point -> OperatorEvaluator.evaluate(config.operator(), point.value(), config.threshold()));
    }
}
