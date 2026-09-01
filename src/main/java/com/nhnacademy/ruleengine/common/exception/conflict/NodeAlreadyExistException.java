package com.nhnacademy.ruleengine.common.exception.conflict;

import com.nhnacademy.ruleengine.common.exception.BaseException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class NodeAlreadyExistException extends BaseException {
    public NodeAlreadyExistException() {
        super(ErrorCode.NODE_ALREADY_EXISTS);
    }
}
