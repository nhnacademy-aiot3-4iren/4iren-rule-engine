package com.nhnacademy.ruleengine.domain.nodeconfig.service.impl;

import com.nhnacademy.ruleengine.common.exception.invalid.InvalidNodeException;
import com.nhnacademy.ruleengine.common.external.service.SensorStaticMetaService;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.repository.NodeRepository;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidateRequest;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidationResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.SensorStaticMeta;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NodeConfigServiceImplTest {

    @Mock private NodeRepository nodeRepository;
    @Mock private SensorStaticMetaService sensorStaticMetaService;
    @Mock private NodeConfigValidatorRegistry validatorRegistry;

    @InjectMocks
    private NodeConfigServiceImpl nodeConfigService;

    @Test
    @DisplayName("액션 노드 조회 시 정적 메타데이터는 null을 반환한다")
    void getNodeConfigNMeta_ActionNode() {
        Node mockNode = mock(Node.class);
        NodeConfig mockConfig = mock(NodeConfig.class);
        when(mockNode.getNodeConfig()).thenReturn(mockConfig);
        when(nodeRepository.findById(1L)).thenReturn(Optional.of(mockNode));

        NodeConfigResponse response = nodeConfigService.getNodeConfigNMeta(100L, 1L, NodeType.ALERT);

        assertThat(response.nodeConfig()).isEqualTo(mockConfig);
        assertThat(response.sensorStaticMetaList()).isNull();
        verify(sensorStaticMetaService, never()).getSensorStaticMetaList(anyLong());
    }

    @Test
    @DisplayName("조건 노드 조회 시 외부 API를 통해 메타데이터 리스트를 가져온다")
    void getNodeConfigNMeta_ConditionNode() {
        Node mockNode = mock(Node.class);
        when(nodeRepository.findById(1L)).thenReturn(Optional.of(mockNode));
        List<SensorStaticMeta> metas = List.of(mock(SensorStaticMeta.class));
        when(sensorStaticMetaService.getSensorStaticMetaList(100L)).thenReturn(metas);

        NodeConfigResponse response = nodeConfigService.getNodeConfigNMeta(100L, 1L, NodeType.THRESHOLD);

        assertThat(response.sensorStaticMetaList()).isEqualTo(metas);
        verify(sensorStaticMetaService).getSensorStaticMetaList(100L);
    }

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
    @DisplayName("NodeConfig Validate 시 에러가 있으면 Failure를 반환한다")
    void validate_Failure() {
        NodeConfig mockConfig = mock(NodeConfig.class);
        when(mockConfig.nodeType()).thenReturn(NodeType.THRESHOLD);
        NodeConfigValidateRequest request = new NodeConfigValidateRequest(mockConfig);

        when(validatorRegistry.validate(eq(NodeType.THRESHOLD), eq(mockConfig), any()))
                .thenReturn(List.of("설정 오류 발생"));

        NodeConfigValidationResponse response = nodeConfigService.validate(100L, request);

        assertThat(response.valid()).isFalse();
        assertThat(response.errors()).containsExactly("설정 오류 발생");
    }

    @Test
    @DisplayName("Validate 시 Config가 null이면 InvalidNodeException이 발생한다")
    void validate_NullConfig() {
        NodeConfigValidateRequest request = new NodeConfigValidateRequest(null);
        assertThatThrownBy(() -> nodeConfigService.validate(100L, request))
                .isInstanceOf(InvalidNodeException.class);
    }
}