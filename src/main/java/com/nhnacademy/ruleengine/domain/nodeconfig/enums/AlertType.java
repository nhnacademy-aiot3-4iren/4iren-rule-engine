package com.nhnacademy.ruleengine.domain.nodeconfig.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AlertType {
    COMFORT_LIMIT_EXCEEDED("긴급","인간의 생체적/물리적 불쾌감이 극에 달해 즉각적인 조치가 필요한 상태"),
    VENTILATION_RECOMMEND("비긴급","실내 이산화탄소나 미세먼지가 정상이지만 환기를 권장하는 가벼운 상태");

    private final String severity;
    private final String typeDesc;
}
