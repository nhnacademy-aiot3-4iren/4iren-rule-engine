package com.nhnacademy.ruleengine.engine.executor.runtimestate;

import com.nhnacademy.ruleengine.engine.executor.ExecutionPath;

import java.util.Deque;
import java.util.Map;
import java.util.Queue;

//실행중인 큐와 Or상태들을 묶은 런타임 상태
public record FlowRuntime(
        Queue<ExecutionPath> queue,
        Map<Long, OrRuntimeState> orStateMap
) {
}
