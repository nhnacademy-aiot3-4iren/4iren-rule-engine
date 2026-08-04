package com.nhnacademy.ruleengine.common.exception.notfound;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class NodeNotFoundException extends BusinessException {
    public NodeNotFoundException(Long nodeId) {
        super(ErrorCode.NODE_NOT_FOUND);
    }
}
