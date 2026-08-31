package com.nhnacademy.ruleengine.domain.nodeconfig.validator.impl;

import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.ThresholdNodeConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ThresholdNodeConfigValidatorTest {
    private final ThresholdNodeConfigValidator validator = new ThresholdNodeConfigValidator();

    @Test
    @DisplayName("지원 타입은 THRESHOLD이다")
    void supportsNodeType() {
        assertThat(validator.supportsNodeType()).isEqualTo(NodeType.THRESHOLD);
    }

    @Test
    @DisplayName("정상 설정은 에러 없음")
    void validate_success() {
        ThresholdNodeConfig config = new ThresholdNodeConfig(NodeType.THRESHOLD, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 25.0);
        List<SensorMetaInfo> metas = List.of(new SensorMetaInfo(MeasurementType.TEMPERATURE, "온도", "실내 온도","C"));
        assertThat(validator.validate(config, metas)).isEmpty();
    }

    @Test
    @DisplayName("센서 메타데이터가 없으면 에러 반환")
    void validate_missingMeasurementType() {
        ThresholdNodeConfig config = new ThresholdNodeConfig(NodeType.THRESHOLD, 0, 0, MeasurementType.CO2, "ppm", Operator.GT, 1000.0);
        List<SensorMetaInfo> metas = List.of(new SensorMetaInfo(MeasurementType.TEMPERATURE, "온도", "실내 온도","C"));
        assertThat(validator.validate(config, metas)).isNotEmpty();
    }
}