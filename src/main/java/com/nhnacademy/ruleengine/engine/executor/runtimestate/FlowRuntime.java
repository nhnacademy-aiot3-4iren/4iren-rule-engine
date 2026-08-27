package com.nhnacademy.ruleengine.engine.executor.runtimestate;

import com.nhnacademy.ruleengine.engine.executor.ExecutionPath;

import java.util.Deque;
import java.util.Map;
import java.util.Queue;

// 노드 실행 중 공유 상태
public record FlowRuntime(
        Map<Long, OrRuntimeState> orStateMap
) {
}
