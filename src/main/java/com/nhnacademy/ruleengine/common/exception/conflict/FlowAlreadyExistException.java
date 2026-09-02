package com.nhnacademy.ruleengine.common.exception.conflict;

import com.nhnacademy.ruleengine.common.exception.BaseException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class FlowAlreadyExistException extends BaseException {
    public FlowAlreadyExistException() {
        super(ErrorCode.FLOW_ALREADY_EXISTS);
    }
}
