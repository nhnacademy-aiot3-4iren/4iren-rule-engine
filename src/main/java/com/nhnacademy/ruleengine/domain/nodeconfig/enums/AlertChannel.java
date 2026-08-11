package com.nhnacademy.ruleengine.domain.nodeconfig.enums;

public enum AlertChannel {
    TELEGRAM("텔레그램");

    private final String channelDesc;
    AlertChannel (String channelDesc){
        this.channelDesc = channelDesc;
    }
}
