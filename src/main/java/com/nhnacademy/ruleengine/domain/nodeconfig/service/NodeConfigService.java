package com.nhnacademy.ruleengine.domain.nodeconfig.service;

import com.nhnacademy.ruleengine.domain.nodeconfig.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidateRequest;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidationResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeMetaResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import org.springframework.stereotype.Service;

@Service
public interface NodeConfigService {

    //nodeId 있을때 nodeConfig 조회
    //nodeId 없을 때 nodeConfig 조회
    //그냥 위의 두 api 합치는 걸로? -> id 음수/양수로 판단
    NodeConfigResponse getNodeConfigNMeta(Long roomId, Long nodeId, NodeType nodeType);

//
//    //nodeConfig검증 api
//    NodeConfigValidationResponse validate(Long roomId, NodeConfigValidateRequest request);


}
