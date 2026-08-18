package com.nhnacademy.ruleengine.engine.executor.node.impl;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.AverageNodeConfig;
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
public class AverageNodeExecutor implements NodeExecutor {

    private final SensorTimeSeriesRepository repository;

    @Override
    public NodeType supportNodeType() {
        return NodeType.AVERAGE;
    }

    @Override
    public NodeExecutionResult execute(ExecutableFlow.ExecutableNode node, FlowContext context, ExecutionPath path, FlowRuntime runtime) {
        AverageNodeConfig config = (AverageNodeConfig) node.nodeConfig();
        Long roomId = context.roomId();

        Instant to = context.triggeredAt();
        Instant from = to.minusSeconds(config.windowSec());

        List<SensorTimeSeriesRepository.TimeSeriesPoint> points = repository.getRange(roomId, config.measurementType(), from, to);

        Double average = calculateAverage(points);

        boolean passed = average != null && OperatorEvaluator.evaluate(config.operator(), average, config.average());
        if(average == null) {
            log.debug("node({}) - roomId({})에 {} 윈도우({}s) 내 데이터 없음. 조건 미충족 처리", node.nodeId(), roomId, config.measurementType(), config.windowSec());
        }

        AlertEvent.NodeResult nodeResult = new AlertEvent.NodeResult(
                node.nodeType().name(),
                config.measurementType().name(),
                config.operator().getSymbol(),
                config.unit(),
                config.average(),
                average
        );

        return NodeExecutionResult.of(passed, path.append(nodeResult));
    }

    private Double calculateAverage(List<SensorTimeSeriesRepository.TimeSeriesPoint> points) {
        if(points.isEmpty()) {
            return null;
        }

        double sum = 0;
        for(SensorTimeSeriesRepository.TimeSeriesPoint point : points) {
            sum += point.value();
        }

        return sum / points.size();
    }
}
