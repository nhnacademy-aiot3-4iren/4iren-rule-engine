package com.nhnacademy.ruleengine.domain.nodeconfig.controller;

import com.nhnacademy.ruleengine.domain.nodeconfig.controller.doc.NodeConfigControllerDoc;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidateRequest;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidationResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.service.NodeConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/rule/rooms/{room-id}")
@RequiredArgsConstructor
@Slf4j
public class NodeConfigController implements NodeConfigControllerDoc {

    private final NodeConfigService nodeConfigService;


    @PostMapping("/validate-config")
    public ResponseEntity<NodeConfigValidationResponse> validateNodeConfig(
            @PathVariable("room-id") Long roomId,
            @RequestBody @Valid NodeConfigValidateRequest request
    ) {
        log.info("노드 설정 검증 요청 roomId={}", roomId);
        NodeConfigValidationResponse response = nodeConfigService.validate(roomId,request);
        return ResponseEntity.ok(response);
    }
}
