package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 一码通订单请求数据
 */
@Data
public class OnebuyOrderReq {

    @ApiModelProperty(value = "订单号")
    private String id;
    @ApiModelProperty(value = "店铺编号")
    private String shop_id;
    @ApiModelProperty(value = "状态：0：未支付，1：已支付，2：失败")
    private String status;
    @ApiModelProperty(value = "金额：单位为分")
    private String amount;
    @ApiModelProperty(value = "备注：不为空，为空的前端会过滤掉")
    private String remark;
    @ApiModelProperty(value = "下单时间，收银台提交订单时间")
    private String order_time;
    @ApiModelProperty(value = "支付时间，已支付的为实际支付时间，其它为收银台后台创建订单的时间")
    private String Payment_time;
    @ApiModelProperty(value = "本系统订单号")
    private String order_no;
}
