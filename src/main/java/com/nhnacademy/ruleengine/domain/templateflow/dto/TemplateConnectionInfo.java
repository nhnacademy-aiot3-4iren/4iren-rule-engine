package com.nhnacademy.ruleengine.domain.templateflow.dto;

import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;
import jakarta.validation.constraints.NotNull;

public record TemplateConnectionInfo(
        @NotNull
        Long sourceNodeId,
        @NotNull
        Long targetNodeId,

        BranchType branchType
) {
        public TemplateConnectionInfo {
                if (branchType == null) {
                        branchType = BranchType.TRUE;
                }
        }
}