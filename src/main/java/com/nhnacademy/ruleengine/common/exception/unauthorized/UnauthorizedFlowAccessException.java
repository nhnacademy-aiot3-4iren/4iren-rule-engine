package com.nhnacademy.ruleengine.common.exception.unauthorized;

import com.nhnacademy.ruleengine.common.exception.BaseException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class UnauthorizedFlowAccessException extends BaseException {
    public UnauthorizedFlowAccessException() {
        super(ErrorCode.UNAUTHORIZED_FLOW_ACCESS );
    }
}
