package com.nhnacademy.ruleengine.common.exception.conflict;

import com.nhnacademy.ruleengine.common.exception.BaseException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class ActiveFlowLimitExceededException extends BaseException {
    public ActiveFlowLimitExceededException() {
        super(ErrorCode.ACTIVE_FLOW_LIMIT_EXCEEDED);
    }
}
