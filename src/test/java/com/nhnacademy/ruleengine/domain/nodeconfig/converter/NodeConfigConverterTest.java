package com.nhnacademy.ruleengine.domain.nodeconfig.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.ThresholdNodeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NodeConfigConverterTest {

    private NodeConfigConverter converter;

    @BeforeEach
    void setUp() {
        converter = new NodeConfigConverter(new ObjectMapper());
    }

    @Test
    @DisplayName("NodeConfig -> JSON")
    void convertToDatabaseColumn_success() {
        NodeConfig config = new ThresholdNodeConfig(NodeType.THRESHOLD, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 25.0);

        String dbData = converter.convertToDatabaseColumn(config);

        assertThat(dbData).isNotBlank();
        assertThat(dbData).contains("THRESHOLD");
        assertThat(dbData).contains("TEMPERATURE");
    }

    @Test
    @DisplayName("NodeConfig(null) -> JSON(null)")
    void convertToDatabaseColumn_null() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    @DisplayName("JSON -> NodeConfig")
    void convertToEntityAttribute_success() {
        String json = "{\"nodeType\":\"THRESHOLD\",\"x\":0,\"y\":0,\"measurementType\":\"TEMPERATURE\",\"unit\":\"C\",\"operator\":\"GT\",\"threshold\":25.0}";

        NodeConfig config = converter.convertToEntityAttribute(json);

        assertThat(config).isInstanceOf(ThresholdNodeConfig.class);
        assertThat(config.nodeType()).isEqualTo(NodeType.THRESHOLD);
        assertThat(config.measurementType()).isEqualTo(MeasurementType.TEMPERATURE);
    }

    @Test
    @DisplayName("DB 데이터(null) -> NodeConfig(null)")
    void convertToEntityAttribute_null() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
        assertThat(converter.convertToEntityAttribute("")).isNull();
        assertThat(converter.convertToEntityAttribute("   ")).isNull();
    }

    @Test
    @DisplayName("잘못된 JSON 문자열 변환 시 IllegalArgumentException")
    void convertToEntityAttribute_invalidJson() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("{ invalid-json }"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
