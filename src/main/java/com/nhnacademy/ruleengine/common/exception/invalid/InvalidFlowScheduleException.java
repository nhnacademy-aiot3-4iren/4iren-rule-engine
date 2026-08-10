package com.nhnacademy.ruleengine.common.exception.invalid;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class InvalidFlowScheduleException extends BusinessException {
    public InvalidFlowScheduleException(Long scheduleId) {
        super( ErrorCode.INVALID_FLOW_SCHEDULE);
    }
}
