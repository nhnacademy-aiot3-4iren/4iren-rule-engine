package com.nhnacademy.ruleengine.common.exception.notfound;

import com.nhnacademy.ruleengine.common.exception.BaseException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class FlowNotFoundException extends BaseException {
    public FlowNotFoundException() {
        super(ErrorCode.FLOW_NOT_FOUND);
    }
}
