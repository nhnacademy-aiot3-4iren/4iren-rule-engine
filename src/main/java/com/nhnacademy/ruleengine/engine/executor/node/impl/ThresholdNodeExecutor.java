package com.nhnacademy.ruleengine.engine.executor.node.impl;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.ThresholdNodeConfig;
import com.nhnacademy.ruleengine.engine.executor.ExecutionPath;
import com.nhnacademy.ruleengine.engine.executor.FlowContext;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutionResult;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutor;
import com.nhnacademy.ruleengine.engine.executor.node.OperatorEvaluator;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.FlowRuntime;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.model.AlertEvent;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ThresholdNodeExecutor implements NodeExecutor {

    @Override
    public NodeType supportNodeType() {
        return NodeType.THRESHOLD;
    }

    @Override
    public NodeExecutionResult execute(ExecutableFlow.ExecutableNode node, FlowContext context, ExecutionPath path, FlowRuntime runtime) {
        ThresholdNodeConfig config = (ThresholdNodeConfig) node.nodeConfig();

        EnvironmentContext.MetricInfo metricInfo = findMetric(context.environmentContext(), config.measurementType());
        Double currentValue = metricInfo != null ? metricInfo.value() : null;

        boolean passed = currentValue != null && OperatorEvaluator.evaluate(config.operator(), currentValue, config.threshold());
        if(currentValue == null) {
            log.debug("node({}) - roomId({})에 {} 최신값 없음. 조건 미충족 처리", node.nodeId(), context.roomId(), config.measurementType());
        }

        AlertEvent.NodeResult nodeResult = new AlertEvent.NodeResult(
                node.nodeType().name(),
                config.measurementType().name(),
                config.operator().getSymbol(),
                config.unit(),
                config.threshold(),
                currentValue
        );

        return NodeExecutionResult.of(passed, path.append(nodeResult));
    }

    private EnvironmentContext.MetricInfo findMetric(EnvironmentContext environmentContext, MeasurementType measurementType) {
        if(environmentContext == null || environmentContext.metrics() == null) {
            return null;
        }
        return environmentContext.metrics().stream()
                .filter(metricInfo -> measurementType.name().equalsIgnoreCase(metricInfo.metric()))
                .findFirst()
                .orElse(null);
    }
}
