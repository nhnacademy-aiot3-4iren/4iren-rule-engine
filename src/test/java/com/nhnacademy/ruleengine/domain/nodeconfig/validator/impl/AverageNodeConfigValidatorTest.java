package com.nhnacademy.ruleengine.domain.nodeconfig.validator.impl;

import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidationResponse.NodeConfigError;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.AverageNodeConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AverageNodeConfigValidatorTest {

    private final AverageNodeConfigValidator validator = new AverageNodeConfigValidator();

    @Test
    @DisplayName("지원 노드 타입은 AVERAGE이다")
    void supportsNodeType() {
        assertThat(validator.supportsNodeType()).isEqualTo(NodeType.AVERAGE);
    }

    @Test
    @DisplayName("정상 설정인 경우 빈 에러 리스트를 반환한다")
    void validate_success() {
        AverageNodeConfig config = new AverageNodeConfig(
                NodeType.AVERAGE, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 25.0, 60
        );
        List<SensorMetaInfo> metas = List.of(new SensorMetaInfo(MeasurementType.TEMPERATURE, "온도", "실내 온도","C"));

        List<NodeConfigError> errors = validator.validate(config, metas);
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("센서 메타데이터에 일치하는 MeasurementType이 없으면 에러가 발생한다")
    void validate_missingMeasurementType() {
        AverageNodeConfig config = new AverageNodeConfig(
                NodeType.AVERAGE, 0, 0, MeasurementType.CO2, "ppm", Operator.GT, 1000.0, 60
        );
        List<SensorMetaInfo> metas = List.of(new SensorMetaInfo(MeasurementType.TEMPERATURE, "온도", "실내 온도","C")); // CO2 없음

        List<NodeConfigError> errors = validator.validate(config, metas);
        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst().message()).contains("지원하지 않는 sensorType");
    }

    @Test
    @DisplayName("windowSec가 범위를 벗어나면 에러가 발생한다")
    void validate_invalidWindowSec() {
        AverageNodeConfig config = new AverageNodeConfig(
                NodeType.AVERAGE, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 25.0, 5 // 10 미만
        );
        List<SensorMetaInfo> metas = List.of(new SensorMetaInfo(MeasurementType.TEMPERATURE, "온도", "실내 온도","C"));

        List<NodeConfigError> errors = validator.validate(config, metas);

        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst().message()).contains("windowSec 범위 초과");
    }
}
