package com.nhnacademy.ruleengine.domain.nodeconfig.enums;


public enum NodeType {
    //condition
    THRESHOLD("임계치"),
    GRADIANT("기울기"),
    AVERAGE("평균값"),
    DURATION("지속시간"),

    //action
    ALERT("알람(텔레그램)")
    ;
    private final String description;

    NodeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSensorDecisionNode() {
        return this == THRESHOLD
                || this == AVERAGE
                || this == DURATION
                || this == GRADIANT;
    }

    public boolean isActionNode() {
        return this == ALERT;
    }
}