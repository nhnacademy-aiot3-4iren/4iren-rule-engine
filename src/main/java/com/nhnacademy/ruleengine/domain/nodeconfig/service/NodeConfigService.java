package com.nhnacademy.ruleengine.domain.nodeconfig.service;

import com.nhnacademy.ruleengine.common.advice.ValidationErrorResponse;
import com.nhnacademy.ruleengine.common.exception.invalid.NodeConfigValidationFailed;
import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
import com.nhnacademy.ruleengine.domain.flow.repository.NodeRepository;
import com.nhnacademy.ruleengine.domain.flow.service.RoomSensorMetaService;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.*;
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
    private final NodeConfigValidatorRegistry validatorRegistry;
    private final RoomSensorMetaService roomSensorMetaService;

    public NodeConfigValidationResponse validate(Long roomId, NodeConfigValidateRequest request) {
        if (request.nodeConfig() == null) {
            throw new NodeConfigValidationFailed(List.of(
                    ValidationErrorResponse.ValidationError.of("nodeConfig", "nodeConfig는 필수입니다.")
            ));
        }
        if (request.nodeConfig().nodeType() == null) {
            throw new NodeConfigValidationFailed(List.of(
                    ValidationErrorResponse.ValidationError.of("nodeConfig.nodeType", "nodeConfig.nodeType은 필수입니다.")
            ));
        }

        // 액션 노드는 sensorMeta 조회 불필요
        List<SensorMetaInfo> sensorMetas = request.nodeConfig().nodeType().isActionNode()
                ? List.of()
                : roomSensorMetaService.getSensorMetaList(roomId);

        List<String> errors = validatorRegistry.validate(
                request.nodeConfig().nodeType(),
                request.nodeConfig(),
                sensorMetas
        );

        if (!errors.isEmpty()) {
            throw new NodeConfigValidationFailed(ValidationErrorResponse.ValidationError.ofList(errors));
//            throw new NodeConfigValidationFailed(errors.stream()
//                    .map(error -> ValidationErrorResponse.ValidationError.of("nodeConfig", error))
//                    .toList());
        }

        return NodeConfigValidationResponse.success();

    }
}
