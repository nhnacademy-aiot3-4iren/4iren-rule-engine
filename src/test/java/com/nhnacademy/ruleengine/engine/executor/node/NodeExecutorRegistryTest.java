package com.nhnacademy.ruleengine.engine.executor.node;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.engine.executor.ExecutionPath;
import com.nhnacademy.ruleengine.engine.executor.FlowContext;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.FlowRuntime;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeExecutorRegistryTest {

    @Mock
    private NodeExecutor thresholdExecutor;

    @Mock
    private NodeExecutor alertExecutor;

    private ExecutableFlow.ExecutableNode node;
    private FlowContext context;
    private ExecutionPath path;
    private FlowRuntime runtime;

    @BeforeEach
    void setUp() {
        node = new ExecutableFlow.ExecutableNode(1L, "node", NodeType.THRESHOLD, null, null);
        path = ExecutionPath.start(1L, null, null);
        runtime = new FlowRuntime(new LinkedList<>(), new HashMap<>());
        context = null; // 이 테스트에서는 위임 여부만 확인하므로 FlowContext 내용은 중요하지 않음
    }

    @Test
    @DisplayName("등록된 nodeType 요청 시 해당 NodeExecutor로 위임한다")
    void execute_delegatesToMatchingExecutor() {
        when(thresholdExecutor.supportNodeType()).thenReturn(NodeType.THRESHOLD);
        NodeExecutorRegistry registry = new NodeExecutorRegistry(List.of(thresholdExecutor));
        registry.init();

        NodeExecutionResult expected = NodeExecutionResult.of(true, path);
        when(thresholdExecutor.execute(node, context, path, runtime)).thenReturn(expected);

        NodeExecutionResult result = registry.execute(NodeType.THRESHOLD, node, context, path, runtime);

        assertThat(result).isEqualTo(expected);
        verify(thresholdExecutor).execute(node, context, path, runtime);
    }

    @Test
    @DisplayName("여러 NodeExecutor가 등록돼 있어도 요청한 nodeType의 NodeExecutor만 호출한다")
    void execute_doesNotCallUnrelatedExecutor() {
        when(thresholdExecutor.supportNodeType()).thenReturn(NodeType.THRESHOLD);
        when(alertExecutor.supportNodeType()).thenReturn(NodeType.ALERT);
        NodeExecutorRegistry registry = new NodeExecutorRegistry(List.of(thresholdExecutor, alertExecutor));
        registry.init();

        when(thresholdExecutor.execute(node, context, path, runtime)).thenReturn(NodeExecutionResult.of(true, path));

        registry.execute(NodeType.THRESHOLD, node, context, path, runtime);

        verify(alertExecutor, never()).execute(any(), any(), any(), any());
    }

    @Test
    @DisplayName("등록되지 않은 nodeType이면 IllegalStateException을 던진다")
    void execute_throwsWhenNodeTypeNotRegistered() {
        when(thresholdExecutor.supportNodeType()).thenReturn(NodeType.THRESHOLD);
        NodeExecutorRegistry registry = new NodeExecutorRegistry(List.of(thresholdExecutor));
        registry.init();

        assertThatThrownBy(() -> registry.execute(NodeType.OR, node, context, path, runtime))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OR");
    }
}