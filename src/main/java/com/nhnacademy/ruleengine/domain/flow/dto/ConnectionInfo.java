package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;
import jakarta.validation.constraints.NotNull;


public record ConnectionInfo(
        @NotNull
        Long sourceNodeId,

        @NotNull
        Long targetNodeId,

        BranchType branchType
) {
        public ConnectionInfo {
                if (branchType == null) {
                        branchType = BranchType.TRUE;
                }
        }
}