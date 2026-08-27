package com.nhnacademy.ruleengine.common.exception.notfound;

import com.nhnacademy.ruleengine.common.exception.BaseException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class FlowScheduleNotFoundException extends BaseException {
    public FlowScheduleNotFoundException() {
        super(ErrorCode.FLOW_SCHEDULE_NOT_FOUND);

    }
}
