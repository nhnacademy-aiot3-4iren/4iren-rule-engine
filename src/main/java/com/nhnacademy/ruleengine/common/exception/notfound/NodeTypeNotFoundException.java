package com.nhnacademy.ruleengine.common.exception.notfound;

import com.nhnacademy.ruleengine.common.exception.BaseException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class NodeTypeNotFoundException extends BaseException {
    public NodeTypeNotFoundException() {
        super(ErrorCode.NODE_TYPE_NOT_FOUND);
    }
}
