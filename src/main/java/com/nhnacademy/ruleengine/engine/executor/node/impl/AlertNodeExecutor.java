package com.nhnacademy.ruleengine.engine.executor.node.impl;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.action.AlertNodeConfig;
import com.nhnacademy.ruleengine.engine.executor.ExecutionPath;
import com.nhnacademy.ruleengine.engine.executor.FlowContext;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutionResult;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutor;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.FlowRuntime;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.model.AlertEvent;
import com.nhnacademy.ruleengine.engine.publisher.AlertEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertNodeExecutor implements NodeExecutor {

    private final AlertEventPublisher alertEventPublisher;

    @Override
    public NodeType supportNodeType() {
        return NodeType.ALERT;
    }

    @Override
    public NodeExecutionResult execute(ExecutableFlow.ExecutableNode node, FlowContext context, ExecutionPath path, FlowRuntime runtime) {
        AlertNodeConfig config = (AlertNodeConfig) node.nodeConfig();

        AlertEvent alertEvent = new AlertEvent(
                context.roomId(),
                config.alertType(),
                config.alertTitle(),
                null,
                null,
                null,
                path.history(),
                context.triggeredAt(),
                UUID.randomUUID().toString()
        );

        log.info("[ALERT] flowId={} roomId={} title={} type={} channel={} history={}",
                context.flowId(), context.roomId(), config.alertTitle(), config.alertType(), config.channel(), path.history());

        alertEventPublisher.publish(alertEvent);
        return NodeExecutionResult.of(true, path);
    }
}
