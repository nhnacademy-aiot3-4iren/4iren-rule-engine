package com.nhnacademy.ruleengine.common.exception.notfound;

import com.nhnacademy.ruleengine.common.exception.BaseException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class FlowTemplateNotFoundException extends BaseException {
    public FlowTemplateNotFoundException() {
        super(ErrorCode.FLOW_TEMPLATE_NOT_FOUND);
    }
}
