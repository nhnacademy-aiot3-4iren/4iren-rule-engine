package com.nhnacademy.ruleengine.common.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

// 베이스
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "node_type"
)
@JsonSubTypes({

})
public abstract class NodeConfig {
    // 공통 필드 있으면 여기

}
