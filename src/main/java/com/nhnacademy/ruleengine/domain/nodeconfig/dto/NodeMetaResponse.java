package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import java.util.List;

public record NodeMetaResponse (
        List<SensorStaticMeta> sensorStaticMetaList
){
    public static NodeMetaResponse of(List<SensorStaticMeta> sensorStaticMetaList) {
        return new NodeMetaResponse(sensorStaticMetaList);
    }
}
