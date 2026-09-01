package com.nhnacademy.ruleengine.domain.nodeconfig.validator.impl;

import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
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
    public List<String> validate(NodeConfig nodeConfig, List<SensorMetaInfo> sensorMetaInfoList) {
        AlertNodeConfig c = (AlertNodeConfig) nodeConfig;
        List<String> errors = new ArrayList<>();

        if (c.x() == null) {
            errors.add("x 좌표는 필수입니다");
        }
        if (c.y() == null) {
            errors.add("y 좌표는 필수입니다");
        }
        // sensorMetas 불필요 (액션 노드)
        if (c.channel() == null) {
            errors.add("알림 채널을 선택해주세요");
        }
        if (c.alertTitle() == null || c.alertTitle().isBlank()) {
            errors.add("알림 제목을 입력해주세요");
        }
        if (c.alertType() == null) {
            errors.add("알림 타입을 선택해주세요");
        }
        if (c.dedupWindowSec() == null) {
            errors.add("dedupWindowSec은 필수입니다");
        } else if (c.dedupWindowSec() <= 0) {
            errors.add("dedupWindowSec은 0보다 커야 합니다");
        }


        return errors;
    }
}
