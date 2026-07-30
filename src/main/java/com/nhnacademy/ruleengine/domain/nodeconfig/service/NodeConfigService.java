package com.nhnacademy.ruleengine.domain.nodeconfig.service;

import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.DevNSensorTypeInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeDefaultConfigResponse;
import org.springframework.stereotype.Service;

@Service
public interface NodeConfigService {

    //노드 설정 조회
    NodeConfigResponse getNodeConfigDetail(Long nodeId);


    //센서 타입별 기본 설정 제공
    NodeDefaultConfigResponse getNodeDefaultConfig(NodeType nodeType, DevNSensorTypeInfo devNSensorTypeInfo);
}
