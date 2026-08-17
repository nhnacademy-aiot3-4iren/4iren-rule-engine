package com.nhnacademy.ruleengine.engine.executor.node;


import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.engine.executor.ExecutionPath;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.FlowRuntime;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.executor.FlowContext;

public interface NodeExecutor {
    NodeType supportNodeType();

    boolean execute(ExecutableFlow.ExecutableNode node, FlowContext context, ExecutionPath path, FlowRuntime runtime);
}
