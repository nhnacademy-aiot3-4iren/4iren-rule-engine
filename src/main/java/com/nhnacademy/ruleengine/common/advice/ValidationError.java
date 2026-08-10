package com.nhnacademy.ruleengine.common.advice;

import java.util.List;

public record ValidationError(
        String code,
        List<String> errors//map<
) {
}
