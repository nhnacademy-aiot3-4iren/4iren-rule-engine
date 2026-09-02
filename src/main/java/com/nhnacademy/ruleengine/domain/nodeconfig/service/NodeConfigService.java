package com.nhnacademy.ruleengine.domain.nodeconfig.service;

import com.nhnacademy.ruleengine.common.exception.invalid.InvalidNodeException;
import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
import com.nhnacademy.ruleengine.domain.flow.service.RoomSensorMetaService;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidateRequest;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidationResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.validator.NodeConfigValidatorRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class NodeConfigService {
    private final NodeConfigValidatorRegistry validatorRegistry;
    private final RoomSensorMetaService roomSensorMetaService;

    public NodeConfigValidationResponse validate(Long roomId, NodeConfigValidateRequest request) {
        if(request.nodeConfig() == null){
            throw new InvalidNodeException();
        }
        if(request.nodeConfig().nodeType() == null){
            throw new InvalidNodeException();
        }

        // 액션 노드는 sensorMeta 조회 불필요
        List<SensorMetaInfo> sensorMetas = request.nodeConfig().nodeType().isActionNode()
                ? List.of()
                : roomSensorMetaService.getSensorMetaList(roomId);

        List<NodeConfigValidationResponse.NodeConfigError> errors = validatorRegistry.validate(
                request.nodeConfig().nodeType(),
                request.nodeConfig(),
                sensorMetas
        );

        if (!errors.isEmpty()) {
            return NodeConfigValidationResponse.failure(errors);
        }

        return NodeConfigValidationResponse.success();

    }
}
