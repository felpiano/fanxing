package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author admin
 * @Date 2024/9/16 21:21
 */
@Data
@ApiModel("商户提交订单")
public class ShopOrderReq implements Serializable {
    @ApiModelProperty("商户ID")
    private String mchid;
    @ApiModelProperty("商户单号")
    private String out_trade_no;
    @ApiModelProperty("下单金额")
    private String amount;
    @ApiModelProperty("支付通道编码")
    private String channel;
    @ApiModelProperty("回调通知URL")
    private String notify_url;
    @ApiModelProperty("同步回调地址")
    private String return_url;
    @ApiModelProperty("时间如20140824030711")
    private String time_stamp;
    @ApiModelProperty("宝转卡，必须传支付宝用户名，否则无法到账其它请填123")
    private String body;
    @ApiModelProperty("签名")
    private String sign;
}
