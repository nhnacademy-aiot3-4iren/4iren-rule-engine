package com.nhnacademy.ruleengine.engine.executor.runtimestate;

import java.util.Map;

// 노드 실행 중 공유 상태
public record FlowRuntime(
        Map<Long, OrRuntimeState> orStateMap
) {
}
