package com.ruoyi.common.enums;

public enum OrderStatus {
    //待支付
    WAIT(0),
    //支付完成
    FINISH(1),
    //订单超时
    TIMEOUT(2),
    //已关闭
    CLOSED(3),
    //待退回
    BACKING(4),
    //已退回
    BACKED(5),
    ;
    private int value;

    OrderStatus(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }
}
