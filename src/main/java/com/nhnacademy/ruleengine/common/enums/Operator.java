package com.nhnacademy.ruleengine.common.enums;

import lombok.Getter;

@Getter
public enum Operator {
    // 비교 연산자
    GT(">"),   // Greater Than        >
    GTE(">="),  // Greater Than Equal  >=
    LT("<"),   // Less Than           <
    LTE("<="),  // Less Than Equal     <=
    EQ("="),   // Equal               =
    NEQ("!=")  // Not Equal           !=
    ;

    private String symbol;

    Operator(String symbol){
        this.symbol = symbol;
    }
}
