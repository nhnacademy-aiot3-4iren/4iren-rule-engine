package com.nhnacademy.ruleengine.domain.nodeconfig.service.impl;

import com.nhnacademy.ruleengine.common.exception.notfound.NodeNotFoundException;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.repository.NodeRepository;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.*;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.action.AlertNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.AverageNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.DurationNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.GradiantNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.ThresholdNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.service.NodeConfigService;
import com.nhnacademy.ruleengine.domain.nodeconfig.service.SensorStaticMetaService;
import com.nhnacademy.ruleengine.domain.nodeconfig.validator.NodeConfigValidatorRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class NodeConfigServiceImpl implements NodeConfigService {
    private final NodeRepository nodeRepository;
    private final SensorStaticMetaService sensorStaticMetaService;
    private final NodeConfigValidatorRegistry validatorRegistry;
    //노드 상세 조회(node_config)
    //nodeId가 음수면 nodeConfig null, nodeType이 행동노드면 sensorStaticMetaList null

    @Override
    public NodeConfigResponse getNodeConfigNMeta(Long roomId, Long nodeId, NodeType nodeType) {
        NodeConfig nodeConfig = null;

        if(nodeId > 0) {
            Node node = nodeRepository.findById(nodeId).orElseThrow(() -> new NodeNotFoundException(nodeId));
            nodeConfig = node.getNodeConfig();
        }

        List<SensorStaticMeta> sensorStaticMetaList = nodeType.isActionNode()
                ? null
                : sensorStaticMetaService.getSensorStaticMetaList(roomId);

        return NodeConfigResponse.of(nodeId,nodeConfig, sensorStaticMetaList);
    }

    @Override
    public NodeConfigValidationResponse validate(Long roomId, NodeConfigValidateRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.nodeConfig().nodeType().isActionNode()) {
            validateAlertConfig((AlertNodeConfig) request.nodeConfig(), errors);
        } else {
            validateConditionConfig(request.nodeConfig(), errors);
        }

        return errors.isEmpty()
                ? NodeConfigValidationResponse.success()
                : NodeConfigValidationResponse.failure(errors);
    }
    private void validateConditionConfig(NodeConfig config, List<String> errors) {
        // 판단 노드 공통: measurementType별 값 범위
        switch (config) {
            case ThresholdNodeConfig c -> {
                validateValueRange(c.measurementType(), c.threshold(), errors);
            }
            case AverageNodeConfig c -> {
                validateValueRange(c.measurementType(), c.average(), errors);
                validateWindowSec(c.windowSec(), errors);
            }
            case GradiantNodeConfig c -> {
                validateValueRange(c.measurementType(), c.gradiant(), errors);
                validateWindowSec(c.windowSec(), errors);
                if (c.gradiant() == 0) errors.add("gradient 값은 0이 될 수 없습니다");
            }
            case DurationNodeConfig c -> {
                validateValueRange(c.measurementType(), c.threshold(), errors);
                if (c.durationSec() < 10 || c.durationSec() > 86400)
                    errors.add("durationSec 범위 초과 (10 ~ 86400): " + c.durationSec());
            }
            default -> errors.add("알 수 없는 노드 타입");
        }
    }
//TODO 검증 코드 클래스별로 분리 -> 확장성
    private void validateValueRange(MeasurementType type, double value, List<String> errors) {
        switch (type) {
            case TEMPERATURE -> {
                if (value < -50 || value > 100)
                    errors.add("온도 값 범위 초과 (-50 ~ 100): " + value);
            }
            case HUMIDITY -> {
                if (value < 0 || value > 100)
                    errors.add("습도 값 범위 초과 (0 ~ 100): " + value);
            }
            case CO2 -> {
                if (value < 0 || value > 10000)
                    errors.add("CO2 값 범위 초과 (0 ~ 10000): " + value);
            }
            // TODO 추가 measurementType 여기에
        }
    }

    private void validateWindowSec(Integer windowSec, List<String> errors) {
        if (windowSec < 10 || windowSec > 3600)
            errors.add("windowSec 범위 초과 (10 ~ 3600): " + windowSec);
    }

    private void validateAlertConfig(AlertNodeConfig config, List<String> errors) {
        if (config.alertTitle() == null || config.alertTitle().isBlank())
            errors.add("알림 제목을 입력해주세요");
        if (config.dedupWindowSec() != null && config.dedupWindowSec() < 0)
            errors.add("dedupWindowSec은 0 이상이어야 합니다");
    }



}
