package com.nhnacademy.ruleengine.common.exception.conflict;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class FlowAlreadyExistException extends BusinessException {
    public FlowAlreadyExistException(Long flowId) {
        super(ErrorCode.FLOW_ALREADY_EXISTS);
    }
}
