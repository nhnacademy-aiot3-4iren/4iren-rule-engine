package com.nhnacademy.ruleengine.domain.nodeconfig.validator.impl;

import com.nhnacademy.ruleengine.domain.nodeconfig.dto.SensorStaticMeta;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.DurationNodeConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class DurationNodeConfigValidatorTest {
    private final DurationNodeConfigValidator validator = new DurationNodeConfigValidator();

    @Test
    @DisplayName("지원 타입은 DURATION이다")
    void supportsNodeType() {
        assertThat(validator.supportsNodeType()).isEqualTo(NodeType.DURATION);
    }

    @Test
    @DisplayName("정상 설정은 에러 없음")
    void validate_success() {
        DurationNodeConfig config = new DurationNodeConfig(NodeType.DURATION, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 25.0, 60);
        List<SensorStaticMeta> metas = List.of(SensorStaticMeta.of(MeasurementType.TEMPERATURE, "C"));
        assertThat(validator.validate(config, metas)).isEmpty();
    }

    @Test
    @DisplayName("센서 메타데이터가 없으면 에러 반환")
    void validate_missingType() {
        DurationNodeConfig config = new DurationNodeConfig(NodeType.DURATION, 0, 0, MeasurementType.CO2, "ppm", Operator.GT, 25.0, 60);
        List<SensorStaticMeta> metas = List.of(SensorStaticMeta.of(MeasurementType.TEMPERATURE, "C"));
        assertThat(validator.validate(config, metas)).isNotEmpty();
    }

    @Test
    @DisplayName("durationSec가 범위를 벗어나면 에러 반환")
    void validate_invalidDurationSec() {
        DurationNodeConfig config = new DurationNodeConfig(NodeType.DURATION, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 25.0, 5); // 10 미만
        List<SensorStaticMeta> metas = List.of(SensorStaticMeta.of(MeasurementType.TEMPERATURE, "C"));
        assertThat(validator.validate(config, metas)).isNotEmpty();
    }
}