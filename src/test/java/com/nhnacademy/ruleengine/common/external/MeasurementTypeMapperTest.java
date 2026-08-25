package com.nhnacademy.ruleengine.common.external;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MeasurementTypeMapperTest {

    private MeasurementTypeMapper measurementTypeMapper;

    @BeforeEach
    void setUp() {
        measurementTypeMapper = new MeasurementTypeMapper();
    }

    @Test
    @DisplayName("문자열 키값으로 MeasurementType 정상 변환")
    void toMeasurementType_Success() {
        assertThat(measurementTypeMapper.toMeasurementType("co2")).contains(MeasurementType.CO2);
        assertThat(measurementTypeMapper.toMeasurementType("TEMPERATURE")).contains(MeasurementType.TEMPERATURE);
    }

    @Test
    @DisplayName("입력값이 null이거나 공백일 경우 빈 Optional 반환")
    void toMeasurementType_NullOrBlank() {
        assertThat(measurementTypeMapper.toMeasurementType(null)).isEmpty();
        assertThat(measurementTypeMapper.toMeasurementType("   ")).isEmpty();
    }

    @Test
    @DisplayName("알 수 없는 키값 입력 시 빈 Optional 반환")
    void toMeasurementType_UnknownKey() {
        assertThat(measurementTypeMapper.toMeasurementType("unknown")).isEmpty();
    }
}