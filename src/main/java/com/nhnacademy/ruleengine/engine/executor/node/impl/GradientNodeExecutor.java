package com.nhnacademy.ruleengine.engine.executor.node.impl;

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
import com.nhnacademy.ruleengine.engine.repository.SensorTimeSeriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

    @Override
    public NodeExecutionResult execute(ExecutableFlow.ExecutableNode node, FlowContext context, ExecutionPath path, FlowRuntime runtime) {
        GradientNodeConfig config = (GradientNodeConfig) node.nodeConfig();
        Long roomId = context.roomId();

        Instant to = context.triggeredAt();
        Instant from = to.minusSeconds(config.windowSec());

        List<SensorTimeSeriesRepository.TimeSeriesPoint> points = repository.getRange(roomId, config.measurementType(), from, to);

        Double gradient = calculateGradient(points);

        boolean passed = gradient != null && OperatorEvaluator.evaluate(config.operator(), gradient, config.gradient());
        if(gradient == null) {
            log.debug("node({}) - roomId({})에 {} 윈도우({}s) 내 기울기 계산 불가. 조건 미충족 처리", node.nodeId(), roomId, config.measurementType(), config.windowSec());
        }

        AlertEvent.NodeResult nodeResult = new AlertEvent.NodeResult(
                node.nodeType().name(),
                config.measurementType().name(),
                config.operator().getSymbol(),
                config.unit(),
                config.gradient(),
                gradient
        );

        return NodeExecutionResult.of(passed, path.append(nodeResult));
    }

    private Double calculateGradient(List<SensorTimeSeriesRepository.TimeSeriesPoint> points) {
        if(points.isEmpty()) {
            return null;
        }

        SensorTimeSeriesRepository.TimeSeriesPoint oldest = points.getFirst();
        SensorTimeSeriesRepository.TimeSeriesPoint latest = points.getLast();

        double elapsedSeconds = Duration.between(oldest.timestamp(), latest.timestamp()).toMillis() / 1000.0;
        if(elapsedSeconds <= 0) {
            return null;
        }

        return (latest.value() - oldest.value()) / elapsedSeconds;
    }
}
