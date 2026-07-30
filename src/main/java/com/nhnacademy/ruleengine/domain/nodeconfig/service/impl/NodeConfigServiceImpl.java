package com.nhnacademy.ruleengine.domain.nodeconfig.service.impl;

import com.nhnacademy.ruleengine.common.exception.notfound.NodeNotFoundException;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;
import com.nhnacademy.ruleengine.domain.flow.repository.NodeRepository;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.DevNSensorTypeInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeDefaultConfigResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.service.NodeConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NodeConfigServiceImpl implements NodeConfigService {
    private final NodeRepository nodeRepository;
    @Override
    public NodeConfigResponse getNodeConfigDetail(Long nodeId) {
        Node node = nodeRepository.findById(nodeId).orElseThrow(()->new NodeNotFoundException(nodeId));
        return  NodeConfigResponse.from(node);
    }

    @Override
    public NodeDefaultConfigResponse getNodeDefaultConfig(NodeType nodeType, DevNSensorTypeInfo devNSensorTypeInfoInfo) {



        return null;
    }
    //노드 상세 조회 및 설정(node_config)
    //+ 센서 타입별 기본설정 제공 및 노드 저장시 검증
    //

}
