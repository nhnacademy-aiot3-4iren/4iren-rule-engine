package com.nhnacademy.ruleengine.domain.nodeconfig.controller;

import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidateRequest;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidationResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.service.NodeConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/rule/rooms/{room-id}")
@RequiredArgsConstructor
public class NodeConfigController {

    private final NodeConfigService nodeConfigService;


    @PostMapping("/validate-config")
    public ResponseEntity<NodeConfigValidationResponse> validateNodeConfig(
            @PathVariable("room-id") Long roomId,
            @RequestBody @Valid NodeConfigValidateRequest request
    ) {
        NodeConfigValidationResponse response = nodeConfigService.validate(roomId,request);
        return ResponseEntity.ok(response);
    }
}
