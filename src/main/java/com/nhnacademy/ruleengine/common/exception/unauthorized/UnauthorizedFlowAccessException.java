package com.nhnacademy.ruleengine.common.exception.unauthorized;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class UnauthorizedFlowAccessException extends BusinessException {
    public UnauthorizedFlowAccessException() {
        super(ErrorCode.UNAUTHORIZED_FLOW_ACCESS );
    }
}
