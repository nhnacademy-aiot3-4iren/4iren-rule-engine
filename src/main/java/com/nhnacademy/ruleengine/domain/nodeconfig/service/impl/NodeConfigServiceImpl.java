package com.nhnacademy.ruleengine.domain.nodeconfig.service.impl;

import com.nhnacademy.ruleengine.common.exception.notfound.NodeNotFoundException;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.repository.NodeRepository;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.*;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.service.NodeConfigService;
import com.nhnacademy.ruleengine.domain.nodeconfig.service.SensorStaticMetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class NodeConfigServiceImpl implements NodeConfigService {
    private final NodeRepository nodeRepository;
    private final SensorStaticMetaService sensorStaticMetaService;
    //노드 상세 조회(node_config)
    //nodeId가 음수면 nodeConfig null, nodeType이 행동노드면 sensorStaticMetaList null

    @Override
    public NodeConfigResponse getNodeConfigNMeta(Long roomId, Long nodeId, NodeType nodeType) {
        NodeConfig nodeConfig = null;

        if(nodeId > 0) {
            Node node = nodeRepository.findById(nodeId).orElseThrow(() -> new NodeNotFoundException(nodeId));
            nodeConfig = node.getNodeConfig();
        }

        List<SensorStaticMeta> sensorStaticMetaList = nodeType.isActionNode()
                ? null
                : sensorStaticMetaService.getSensorStaticMetaList(roomId);

        return NodeConfigResponse.of(nodeId,nodeConfig, sensorStaticMetaList);
    }

}
