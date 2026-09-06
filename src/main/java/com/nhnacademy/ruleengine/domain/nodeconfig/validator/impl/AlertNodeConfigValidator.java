package com.nhnacademy.ruleengine.domain.nodeconfig.validator.impl;

import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidationResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.action.AlertNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.validator.NodeConfigValidator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AlertNodeConfigValidator implements NodeConfigValidator {
    @Override
    public NodeType supportsNodeType() {
        return NodeType.ALERT;
    }

    @Override
    public List<NodeConfigValidationResponse.NodeConfigError> validate(NodeConfig nodeConfig, List<SensorMetaInfo> sensorMetaInfoList) {
        AlertNodeConfig c = (AlertNodeConfig) nodeConfig;
        List<NodeConfigValidationResponse.NodeConfigError> errors = new ArrayList<>();

        // sensorMetas 불필요 (액션 노드)

        if (c.alertTitle() == null || c.alertTitle().isBlank()) {
            errors.add(NodeConfigValidationResponse.NodeConfigError.of("nodeConfig.alertTitle", "알림 제목을 입력해야 합니다."));
        }
        if (c.alertType() == null) {
            errors.add(NodeConfigValidationResponse.NodeConfigError.of("nodeConfig.alertType", "알림 종류를 선택해야 합니다."));
        }

        if (c.dedupWindowSec() == null) {
            errors.add(NodeConfigValidationResponse.NodeConfigError.of("nodeConfig.dedupWindowSec","중복 알림 제한 시간을 설정해야 합니다."));
        } else if (c.dedupWindowSec() <= 0) {
            errors.add(NodeConfigValidationResponse.NodeConfigError.of("nodeConfig.dedupWindowSec","중복 알림 제한 시간은 1초 이상으로 설정해야 합니다."));
        }


        return errors;
    }
}
