package com.nhnacademy.ruleengine.domain.nodeconfig.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.domain.nodeconfig.NodeConfig;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

@Converter
@Component
public class NodeConfigConverter implements AttributeConverter<NodeConfig, String> {

    private static ObjectMapper objectMapper;
    public NodeConfigConverter(ObjectMapper objectMapper){
        NodeConfigConverter.objectMapper = objectMapper;
    }

    @Override
    public String convertToDatabaseColumn(NodeConfig nodeConfig) {
        if(nodeConfig == null) {
            return null;
        }

        try{
            return objectMapper.writeValueAsString(nodeConfig);
        } catch (Exception e) {
        throw new IllegalArgumentException("MySQL JSON 직렬화 실패", e);
        }
    }

    @Override
    public NodeConfig convertToEntityAttribute(String dbData) {
        if(dbData == null || dbData.isBlank()){
            return null;
        }

        try {
            return objectMapper.readValue(dbData, NodeConfig.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("MySQL JSON 역직렬화 실패", e);
        }
    }
}
