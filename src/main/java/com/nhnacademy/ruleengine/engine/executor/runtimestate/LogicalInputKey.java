package com.nhnacademy.ruleengine.engine.executor.runtimestate;

import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;

//조건 노드에
public record LogicalInputKey(
        Long fromNodeId,
        BranchType branchType,
        Long toNodeId
){

}
