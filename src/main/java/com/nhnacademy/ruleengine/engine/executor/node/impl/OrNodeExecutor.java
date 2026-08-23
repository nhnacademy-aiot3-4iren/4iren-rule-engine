package com.nhnacademy.ruleengine.engine.executor.node.impl;

import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.engine.executor.ExecutionPath;
import com.nhnacademy.ruleengine.engine.executor.FlowContext;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutionResult;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutor;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.FlowRuntime;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.LogicalInputKey;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.OrRuntimeState;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.model.AlertEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class OrNodeExecutor implements NodeExecutor {

    @Override
    public NodeType supportNodeType() {
        return NodeType.OR;
    }

    @Override
    public NodeExecutionResult execute(ExecutableFlow.ExecutableNode node, FlowContext context, ExecutionPath path, FlowRuntime runtime) {
        Long orNodeId = node.nodeId();

        OrRuntimeState state = runtime.orStateMap().computeIfAbsent(orNodeId, id -> new OrRuntimeState(id, resolveInputs(context.flow(), id)));

        LogicalInputKey arrivedKey = new LogicalInputKey(path.fromNodeId(), path.fromBranchType(), orNodeId);
        state.markArrived(arrivedKey, path.history());

        if(!state.isReady()) {
            return NodeExecutionResult.of(false, path);
        }
        boolean passed = state.isSatisfied();

        log.debug("node({}) OR 판단 - arrivedKey={}, satisfied={}, ready={}", orNodeId, arrivedKey, passed, state.isReady());

        List<AlertEvent.NodeResult> mergedNodeResults = state.mergeArrivedHistories();


        AlertEvent.NodeResult nodeResult = new AlertEvent.NodeResult(
                node.nodeType().name(),
                null,
                null,
                null,
                null,
                null
        );
        mergedNodeResults.add(nodeResult);

        return NodeExecutionResult.of(passed, path.appendMergedResult(mergedNodeResults));

    }

    private List<LogicalInputKey> resolveInputs(ExecutableFlow flow, Long orNodeId) {
        List<LogicalInputKey> inputs = new ArrayList<>();

        flow.trueAdjacencyMap().forEach((fromNodeId, targetNodeIds) -> {
            if(targetNodeIds.contains(orNodeId)) {
                inputs.add(new LogicalInputKey(fromNodeId, BranchType.TRUE, orNodeId));
            }
        });

        flow.falseAdjacencyMap().forEach((fromNodeId, targetNodeIds) -> {
            if(targetNodeIds.contains(orNodeId)) {
                inputs.add(new LogicalInputKey(fromNodeId, BranchType.FALSE, orNodeId));
            }
        });

        return inputs;
    }
}
