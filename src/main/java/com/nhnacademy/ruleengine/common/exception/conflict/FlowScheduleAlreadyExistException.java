package com.nhnacademy.ruleengine.common.exception.conflict;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class FlowScheduleAlreadyExistException extends BusinessException {
    public FlowScheduleAlreadyExistException() {
        super(ErrorCode.FLOW_SCHEDULE_ALREADY_EXISTS);
    }
}
