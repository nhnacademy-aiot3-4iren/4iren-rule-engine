package com.nhnacademy.ruleengine.common.exception.conflict;

import com.nhnacademy.ruleengine.common.exception.BaseException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class FlowScheduleAlreadyExistException extends BaseException {
    public FlowScheduleAlreadyExistException() {
        super(ErrorCode.FLOW_SCHEDULE_ALREADY_EXISTS);
    }
}
