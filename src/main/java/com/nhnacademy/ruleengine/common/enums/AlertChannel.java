package com.nhnacademy.ruleengine.common.enums;

public enum AlertChannel {
    TELEGRAM("텔레그램");

    private String channelDesc;
    AlertChannel (String channelDesc){
        this.channelDesc = channelDesc;
    }
}
