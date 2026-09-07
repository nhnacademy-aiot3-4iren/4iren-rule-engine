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

        log.info("알림 노드 실행 flowId={}, roomId={}, nodeId={}, title={}, type={}",
                context.flowId(), context.roomId(), node.nodeId(), config.alertTitle(), config.alertType());
        log.info("알림 노드 실행 이력 flowId={}, nodeId={}, history={}", context.flowId(), node.nodeId(), path.history());

        alertEventPublisher.publish(alertEvent, node.nodeId(), config.dedupWindowSec());
        return NodeExecutionResult.of(true, path);
    }
}
