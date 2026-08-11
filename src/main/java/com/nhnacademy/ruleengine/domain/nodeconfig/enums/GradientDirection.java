package com.nhnacademy.ruleengine.domain.nodeconfig.enums;

public enum GradientDirection {
    UP("상승 추세 검증"),
    DOWN("하락 추세 검증"),
    ABS("절대 변화량 검증")//방향 상관없이 변화의 크기만 봄
    ;

    private final String directionStr;
    GradientDirection(String directionStr){
        this.directionStr = directionStr;
    }
}
