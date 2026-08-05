package com.nhnacademy.ruleengine.domain.flow.dto.flow;

import com.nhnacademy.ruleengine.domain.flow.dto.connection.ConnectionInfo;
import com.nhnacademy.ruleengine.domain.flow.dto.node.NodeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record FlowCreateRequest(
        @NotBlank
        @Length(max = 50)
        String flowName,

        @Length(max = 255)
        String description,

        @NotEmpty
        List<NodeInfo> nodes,

        @NotNull
        List<ConnectionInfo> connections
) {
}
