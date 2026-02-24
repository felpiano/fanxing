package com.ruoyi.system.telegram.tgDataEntity;

import java.util.Date;

public class TgOrderInfo {
    //商户单号
    public String clientOrderNo;
    //系统单号
    public String orderNo;
    //商户消息id
    public int shMessageId;
    //商户群组id
    public String shChatId;
    //平台消息id
    public int ptMessageId;
    //平台群组id
    public String ptChatId;
    //消息发送日期
    public Date messageDate;
    public String getClientOrderNo() {
        return clientOrderNo;
    }

    public void setClientOrderNo(String clientOrderNo) {
        this.clientOrderNo = clientOrderNo;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public int getShMessageId() {
        return shMessageId;
    }

    public void setShMessageId(int shMessageId) {
        this.shMessageId = shMessageId;
    }

    public String getShChatId() {
        return shChatId;
    }

    public void setShChatId(String shChatId) {
        this.shChatId = shChatId;
    }

    public int getPtMessageId() {
        return ptMessageId;
    }

    public void setPtMessageId(int ptMessageId) {
        this.ptMessageId = ptMessageId;
    }

    public String getPtChatId() {
        return ptChatId;
    }

    public void setPtChatId(String ptChatId) {
        this.ptChatId = ptChatId;
    }

    public Date getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(Date messageDate) {
        this.messageDate = messageDate;
    }
}
