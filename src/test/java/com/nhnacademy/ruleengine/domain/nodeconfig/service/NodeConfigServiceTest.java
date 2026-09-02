package com.nhnacademy.ruleengine.domain.nodeconfig.service;

import com.nhnacademy.ruleengine.common.exception.invalid.InvalidNodeException;
import com.nhnacademy.ruleengine.domain.flow.service.RoomSensorMetaService;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidateRequest;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidationResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidationResponse.NodeConfigError;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.validator.NodeConfigValidatorRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeConfigServiceTest {

    @Mock private RoomSensorMetaService roomSensorMetaService;
    @Mock private NodeConfigValidatorRegistry validatorRegistry;

    @InjectMocks
    private NodeConfigService nodeConfigService;


    @Test
    @DisplayName("NodeConfig Validate 시 에러가 없으면 Success를 반환한다")
    void validate_Success() {
        NodeConfig mockConfig = mock(NodeConfig.class);
        when(mockConfig.nodeType()).thenReturn(NodeType.THRESHOLD);
        NodeConfigValidateRequest request = new NodeConfigValidateRequest(mockConfig);

        when(validatorRegistry.validate(eq(NodeType.THRESHOLD), eq(mockConfig), any()))
                .thenReturn(List.of()); // 에러 없음

        NodeConfigValidationResponse response = nodeConfigService.validate(100L, request);

        assertThat(response.valid()).isTrue();
        assertThat(response.errors()).isEmpty();
    }

    @Test
    @DisplayName("NodeConfig Validate 시 에러가 있으면 failure 응답을 반환한다")
    void validate_Failure() {
        NodeConfig mockConfig = mock(NodeConfig.class);
        when(mockConfig.nodeType()).thenReturn(NodeType.THRESHOLD);
        NodeConfigValidateRequest request = new NodeConfigValidateRequest(mockConfig);

        when(validatorRegistry.validate(eq(NodeType.THRESHOLD), eq(mockConfig), any()))
                .thenReturn(List.of(NodeConfigError.of("nodeConfig.threshold", "설정 오류 발생")));

        NodeConfigValidationResponse response = nodeConfigService.validate(100L, request);

        assertThat(response.valid()).isFalse();
        assertThat(response.errors()).containsExactly(NodeConfigError.of("nodeConfig.threshold", "설정 오류 발생"));
    }

    @Test
    @DisplayName("Validate 시 Config가 null이면 InvalidNodeException이 발생한다")
    void validate_NullConfig() {
        NodeConfigValidateRequest request = new NodeConfigValidateRequest(null);

        assertThatThrownBy(() -> nodeConfigService.validate(100L, request))
                .isInstanceOf(InvalidNodeException.class);
    }

}
