package com.nhnacademy.ruleengine.domain.nodeconfig.service;

import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidateRequest;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidationResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;

public interface NodeConfigService {

    NodeConfigResponse getNodeConfigNMeta(Long roomId, Long nodeId, NodeType nodeType);
//    //nodeConfig검증 api
    NodeConfigValidationResponse validate(Long roomId, NodeConfigValidateRequest request);
}
