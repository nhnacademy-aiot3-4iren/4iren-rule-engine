package com.nhnacademy.ruleengine.common.exception.invalid;

import com.nhnacademy.ruleengine.common.exception.BaseException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class InvalidFlowScheduleException extends BaseException {
    public InvalidFlowScheduleException(Long scheduleId) {
        super( ErrorCode.INVALID_FLOW_SCHEDULE);
    }
}
