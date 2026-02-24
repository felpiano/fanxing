package com.ruoyi.common.enums;

public enum BusinessConfigKey {

    FORWARD_URL("business.forwardUrl"),
    ORDER_OVERTIME("business.orderOvertime"),
    SUBMITORDERURL("business.submitOrderUrl"),
    CLIENTIP("business.clientip"),
    BUSINESSIMG("business.img"),
   ;

    private String configKey;

    BusinessConfigKey(String configKey){
        this.configKey = configKey;
    };

    public String getConfigKey() {
        return configKey;
    }
}
