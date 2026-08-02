package com.nhnacademy.ruleengine.domain.nodeconfig.controller;

import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidateRequest;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidationResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.service.NodeConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/rooms/{room-id}")
@RequiredArgsConstructor
public class NodeConfigController {

    private final NodeConfigService nodeConfigService;


    @GetMapping("/node-config/{node-id}")
    public NodeConfigResponse getNodeConfigNMeta(
            @PathVariable("room-id") Long roomId,
            @PathVariable("node-id") Long nodeId,
            @RequestParam NodeType nodeType
    ) {
        return nodeConfigService.getNodeConfigNMeta(roomId, nodeId, nodeType);
    }

//    @PostMapping("/validate-config")
//    public NodeConfigValidationResponse validateNodeConfig(
//            @PathVariable("room-id") Long roomId,
//            @RequestBody NodeConfigValidateRequest request
//    ) {
//        return nodeConfigService.validate(roomId,request);
//    }
}
