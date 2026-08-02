package com.nhnacademy.ruleengine.domain.nodeconfig.enums;

public enum GradiantDirection {
    UP("상승 추세 검증"),
    DOWN("하락 추세 검증"),
    ABS("절대 변화량 검증")//방향 상관없이 변화의 크기만 봄
    ;

    private String directionStr;
    GradiantDirection(String directionStr){
        this.directionStr = directionStr;
    }
}
