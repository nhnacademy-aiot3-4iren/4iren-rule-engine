package com.nhnacademy.ruleengine.domain.nodeconfig.service;

import com.nhnacademy.ruleengine.domain.flow.dto.node.NodeResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeDefaultConfigResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigUpdateRequest;
import org.springframework.stereotype.Service;

@Service
public interface NodeConfigService {

    //노드 설정 조회
    NodeResponse nodeConfigDetail(Long nodeId, NodeDefaultConfigResponse request);

    //노드 설정 수정 및 저장
    void updateNodeConfig(Long roomId, Long flowId, Long tempNodeId, NodeConfigUpdateRequest request);
}
