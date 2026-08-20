package com.nhnacademy.ruleengine.common.exception.notfound;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class FlowTemplateNotFoundException extends BusinessException {
    public FlowTemplateNotFoundException() {
        super(ErrorCode.FLOW_TEMPLATE_NOT_FOUND);
    }
}
