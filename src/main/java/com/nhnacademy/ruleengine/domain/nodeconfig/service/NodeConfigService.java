package com.nhnacademy.ruleengine.domain.nodeconfig.service;

import com.nhnacademy.ruleengine.common.exception.invalid.InvalidNodeException;
import com.nhnacademy.ruleengine.common.exception.notfound.NodeNotFoundException;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.repository.NodeRepository;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.*;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.validator.NodeConfigValidatorRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class NodeConfigService {
    private final NodeRepository nodeRepository;
    private final SensorStaticMetaService sensorStaticMetaService;
    private final NodeConfigValidatorRegistry validatorRegistry;
    //노드 상세 조회(node_config)
    //nodeId가 음수면 nodeConfig null, nodeType이 행동노드면 sensorStaticMetaList null


    public NodeConfigResponse getNodeConfigNMeta(Long roomId, Long nodeId, NodeType nodeType) {
        NodeConfig nodeConfig = null;

        if(nodeId != null && nodeId > 0) {
            Node node = nodeRepository.findById(nodeId).orElseThrow(NodeNotFoundException::new);
            nodeConfig = node.getNodeConfig();
        }

        List<SensorStaticMeta> sensorStaticMetaList = nodeType.isConditionNode()
                ? sensorStaticMetaService.getSensorStaticMetaList(roomId)
                : null;

        return NodeConfigResponse.of(nodeId,nodeConfig, sensorStaticMetaList);
    }
    public NodeConfigValidationResponse validate(Long roomId, NodeConfigValidateRequest request) {
        if (request.nodeConfig() == null) {
            throw new InvalidNodeException();
        }

        // 액션 노드는 sensorMeta 조회 불필요
        List<SensorStaticMeta> sensorMetas = request.nodeConfig().nodeType().isActionNode()
                ? List.of()
                : sensorStaticMetaService.getSensorStaticMetaList(roomId);

        List<String> errors = validatorRegistry.validate(
                request.nodeConfig().nodeType(),
                request.nodeConfig(),
                sensorMetas
        );

        return errors.isEmpty()
                ? NodeConfigValidationResponse.success()
                : NodeConfigValidationResponse.failure(errors);

    }
}
