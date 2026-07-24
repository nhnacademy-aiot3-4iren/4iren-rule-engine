package com.nhnacademy.ruleengine.common.exception.invalid;

public class InvalidFlowScheduleException extends RuntimeException {
    public InvalidFlowScheduleException(Long scheduleId) {
        super( " "+scheduleId);
    }
}
