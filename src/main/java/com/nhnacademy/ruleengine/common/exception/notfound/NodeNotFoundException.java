package com.nhnacademy.ruleengine.common.exception.notfound;

import com.nhnacademy.ruleengine.common.exception.BaseException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class NodeNotFoundException extends BaseException {
    public NodeNotFoundException() {
        super(ErrorCode.NODE_NOT_FOUND);
    }
}
