package com.nhnacademy.ruleengine.common.exception.notfound;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class FlowNotFoundException extends BusinessException {
    public FlowNotFoundException() {
        super(ErrorCode.FLOW_NOT_FOUND);
    }
}
