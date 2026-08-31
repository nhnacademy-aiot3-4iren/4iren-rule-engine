package com.nhnacademy.ruleengine.domain.nodeconfig.validator.impl;

import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.GradientNodeConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class GradientNodeConfigValidatorTest {
    private final GradientNodeConfigValidator validator = new GradientNodeConfigValidator();

    @Test
    @DisplayName("지원 타입은 GRADIENT이다")
    void supportsNodeType() {
        assertThat(validator.supportsNodeType()).isEqualTo(NodeType.GRADIENT);
    }

    @Test
    @DisplayName("정상 설정인 경우 빈 에러 반환")
    void validate_success() {
        GradientNodeConfig config = new GradientNodeConfig(NodeType.GRADIENT, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 1.5, 60);
        List<SensorMetaInfo> metas = List.of(new SensorMetaInfo(MeasurementType.TEMPERATURE, "온도", "실내 온도","C"));
        assertThat(validator.validate(config, metas)).isEmpty();
    }

    @Test
    @DisplayName("센서 메타데이터가 없으면 에러 반환")
    void validate_missingType() {
        GradientNodeConfig config = new GradientNodeConfig(NodeType.GRADIENT, 0, 0, MeasurementType.CO2, "ppm", Operator.GT, 1.5, 60);
        List<SensorMetaInfo> metas = List.of(new SensorMetaInfo(MeasurementType.TEMPERATURE, "온도", "실내 온도","C")); // 온도 메타데이터만 존재
        assertThat(validator.validate(config, metas)).isNotEmpty();
    }

    @Test
    @DisplayName("windowSec 범위를 벗어나면 에러 반환")
    void validate_invalidWindowSec() {
        GradientNodeConfig config = new GradientNodeConfig(NodeType.GRADIENT, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 1.5, 5); // 10 미만
        List<SensorMetaInfo> metas = List.of(new SensorMetaInfo(MeasurementType.TEMPERATURE, "온도", "실내 온도","C"));
        assertThat(validator.validate(config, metas)).isNotEmpty();
    }

    @Test
    @DisplayName("gradient 값이 0이면 에러 반환")
    void validate_zeroGradient() {
        GradientNodeConfig config = new GradientNodeConfig(NodeType.GRADIENT, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 0.0, 60);
        List<SensorMetaInfo> metas = List.of(new SensorMetaInfo(MeasurementType.TEMPERATURE, "온도", "실내 온도","C"));
        assertThat(validator.validate(config, metas)).isNotEmpty();
    }
}