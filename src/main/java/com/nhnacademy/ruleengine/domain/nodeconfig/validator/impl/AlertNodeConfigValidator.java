package com.nhnacademy.ruleengine.domain.nodeconfig.validator.impl;

import com.nhnacademy.ruleengine.domain.nodeconfig.dto.SensorStaticMeta;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.action.AlertNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.validator.NodeConfigValidator;

import java.util.ArrayList;
import java.util.List;

public class AlertNodeConfigValidator implements NodeConfigValidator {
    @Override
    public NodeType supportsNodeType() {
        return NodeType.ALERT;
    }

    @Override
    public List<String> validate(NodeConfig nodeConfig, List<SensorStaticMeta> sensorStaticMetaList) {
        AlertNodeConfig c = (AlertNodeConfig) nodeConfig;
        List<String> errors = new ArrayList<>();

        // sensorMetas 불필요 (액션 노드)
        if (c.channel() == null) {
            errors.add("알림 채널을 선택해주세요");
        }
        if (c.alertTitle() == null || c.alertTitle().isBlank()) {
            errors.add("알림 제목을 입력해주세요");
        }
        if (c.dedupWindowSec() != null && c.dedupWindowSec() < 0) {
            errors.add("dedupWindowSec은 0 이상이어야 합니다");
        }

        return errors;
    }
}
