package com.nhnacademy.ruleengine.common.exception.notfound;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class NodeTypeNotFoundException extends BusinessException {
    public NodeTypeNotFoundException() {
        super(ErrorCode.NODE_TYPE_NOT_FOUND);
    }
}
