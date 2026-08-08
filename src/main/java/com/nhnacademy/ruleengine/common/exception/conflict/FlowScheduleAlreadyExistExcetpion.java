package com.nhnacademy.ruleengine.common.exception.conflict;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class FlowScheduleAlreadyExistExcetpion extends BusinessException {
    public FlowScheduleAlreadyExistExcetpion() {
        super(ErrorCode.FLOW_SCHEDULE_ALREADY_EXISTS);
    }
}
