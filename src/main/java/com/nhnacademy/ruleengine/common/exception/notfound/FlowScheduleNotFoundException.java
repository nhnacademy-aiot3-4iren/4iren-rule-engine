package com.nhnacademy.ruleengine.common.exception.notfound;

public class FlowScheduleNotFoundException extends RuntimeException {
    public FlowScheduleNotFoundException(Long flowScheduleId) {
        super("FlowSchedule Not Found: "+ flowScheduleId);

    }
}
