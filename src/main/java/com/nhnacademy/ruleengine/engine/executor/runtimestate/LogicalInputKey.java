package com.nhnacademy.ruleengine.engine.executor.runtimestate;

import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;

//논리 노드로 연결된 연결들의 정보
public record LogicalInputKey(
        Long fromNodeId,
        BranchType branchType,
        Long toNodeId
){

}
