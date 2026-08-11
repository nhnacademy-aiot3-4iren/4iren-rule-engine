package com.nhnacademy.ruleengine.domain.nodeconfig.enums;


import lombok.Getter;

@Getter
public enum NodeType {
    //condition
    THRESHOLD("임계치 판단"),
    GRADIENT("기울기 판단"),
    AVERAGE("평균값 판단"),
    DURATION("지속시간 판단"),

    // logical
    OR("OR 조건"),

    //action
    ALERT("알람(텔레그램)");

    private final String description;

    NodeType(String description) {
        this.description = description;
    }

    public boolean isConditionNode() {
        return this == THRESHOLD
                || this == AVERAGE
                || this == DURATION
                || this == GRADIENT;
    }

    public boolean isActionNode() {
        return this == ALERT;
    }
}