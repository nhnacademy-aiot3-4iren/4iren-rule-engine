package com.nhnacademy.ruleengine.domain.nodeconfig.service;

import com.nhnacademy.ruleengine.common.exception.invalid.InvalidNodeException;
import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
import com.nhnacademy.ruleengine.domain.flow.service.RoomSensorMetaService;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidateRequest;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidationResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.validator.NodeConfigValidatorRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
@Slf4j
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
        log.info("노드 설정 검증 시작 roomId={}, nodeType={}", roomId, request.nodeConfig().nodeType());

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
            log.info("노드 설정 검증 실패 roomId={}, nodeType={}, errorCount={}",
                    roomId, request.nodeConfig().nodeType(), errors.size());
            return NodeConfigValidationResponse.failure(errors);
        }

        log.info("노드 설정 검증 성공 roomId={}, nodeType={}", roomId, request.nodeConfig().nodeType());
        return NodeConfigValidationResponse.success();

    }
}
