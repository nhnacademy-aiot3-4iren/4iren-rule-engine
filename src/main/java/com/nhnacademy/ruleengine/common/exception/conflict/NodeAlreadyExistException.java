package com.nhnacademy.ruleengine.common.exception.conflict;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class NodeAlreadyExistException extends BusinessException {
    public NodeAlreadyExistException(Long noedId) {
        super(ErrorCode.NODE_ALREADY_EXISTS);
    }
}
