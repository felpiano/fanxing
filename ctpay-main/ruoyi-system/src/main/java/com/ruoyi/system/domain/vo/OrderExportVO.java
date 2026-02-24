package com.ruoyi.system.domain.vo;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderExportVO {
    private String shopName;
    private String orderNo;
    private String traceNo;
    private String amount;
    private String realAmount;
    private String channelCode;
    private String channelName;
    private String orderStatus;
    private String callbackStatus;
    private String platName;
    private String orderTime;
    private String payTime;
    private String finishTime;
    private String cardNo;
    private String cardPassword;
    private String shopRateFee;
    private String platRateFee;
    private String shopCostRate;
    private String platCostRate;
    private String realFaceValue;
    private String settleAmount;
    private String forwardUrl;
}
