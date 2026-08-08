package com.nhnacademy.ruleengine.common.exception.notfound;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class FlowScheduleNotFoundException extends BusinessException {
    public FlowScheduleNotFoundException() {
        super(ErrorCode.FLOW_SCHEDULE_NOT_FOUND);

    }
}
