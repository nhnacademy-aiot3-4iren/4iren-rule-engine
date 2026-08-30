package com.nhnacademy.ruleengine.engine.executor.node;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.engine.executor.ExecutionPath;
import com.nhnacademy.ruleengine.engine.executor.FlowContext;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.FlowRuntime;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NodeExecutorRegistry {
    private final List<NodeExecutor> nodeExecutors;
    private final Map<NodeType, NodeExecutor> registry = new HashMap<>();

    @PostConstruct
    public void init(){
        nodeExecutors.forEach(
                ne -> registry.put(ne.supportNodeType(), ne)
        );
    }

    public NodeExecutionResult execute(NodeType nodeType, ExecutableFlow.ExecutableNode node, FlowContext context, ExecutionPath path, FlowRuntime runtime){
        NodeExecutor nodeExecutor = registry.get(nodeType);
        if(nodeExecutor == null){
            throw new IllegalStateException("NodeExecutor 없음. nodeType:" + nodeType);
        }
        return nodeExecutor.execute(node, context, path, runtime);
    }
}
