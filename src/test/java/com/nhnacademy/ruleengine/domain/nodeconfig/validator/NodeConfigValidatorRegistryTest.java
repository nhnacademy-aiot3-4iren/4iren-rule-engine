package com.nhnacademy.ruleengine.domain.nodeconfig.validator;

import com.nhnacademy.ruleengine.common.exception.notfound.NodeTypeNotFoundException;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class NodeConfigValidatorRegistryTest {
    private NodeConfigValidatorRegistry registry;
    private NodeConfigValidator validator;

    @BeforeEach
    void setUp() {
        validator = mock(NodeConfigValidator.class);
        when(validator.supportsNodeType()).thenReturn(NodeType.THRESHOLD);
        registry = new NodeConfigValidatorRegistry(List.of(validator));
        registry.init();
    }

    @Test
    @DisplayName("등록된 Validator를 통해 검증을 수행한다")
    void validate_success() {
        NodeConfig config = mock(NodeConfig.class);
        when(validator.validate(config, List.of())).thenReturn(List.of());

        List<String> errors = registry.validate(NodeType.THRESHOLD, config, List.of());

        assertThat(errors).isEmpty();
        verify(validator).validate(config, List.of());
    }

    @Test
    @DisplayName("등록되지 않은 노드 타입 검증 시 NodeTypeNotFoundException 발생")
    void validate_notFound() {
        NodeConfig config = mock(NodeConfig.class);

        assertThatThrownBy(() -> registry.validate(NodeType.ALERT, config, List.of()))
                .isInstanceOf(NodeTypeNotFoundException.class);
    }
}