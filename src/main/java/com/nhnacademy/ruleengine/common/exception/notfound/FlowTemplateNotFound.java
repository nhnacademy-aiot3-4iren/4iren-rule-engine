package com.nhnacademy.ruleengine.common.exception.notfound;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class FlowTemplateNotFound extends BusinessException {
    public FlowTemplateNotFound(Long templateId) {
        super(ErrorCode.FLOW_TEMPLATE_NOT_FOUND);
    }
}
