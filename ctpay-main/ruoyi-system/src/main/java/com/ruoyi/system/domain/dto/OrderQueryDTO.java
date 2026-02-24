package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("订单查询")
public class OrderQueryDTO extends BasePageDTO{
    @ApiModelProperty("商户订单号")
    private String shopOrderNo;

    @ApiModelProperty("系统订单号")
    private String tradeNo;

    @ApiModelProperty("通道ID")
    private Long channelId;

    @ApiModelProperty("通道编码")
    private String channelCode;

    @ApiModelProperty("通道名称")
    private String channelName;

    @ApiModelProperty("收款人")
    private String recipient;

    @ApiModelProperty("账号备注")
    private String accountRemark;

    @ApiModelProperty("付款人")
    private String payer;

    @ApiModelProperty("订单状态：0-待支付；1-支付成功；2-订单超时；3-已关闭")
    private Integer orderStatus;

    @ApiModelProperty("订单备注")
    private String orderRemark;

    @ApiModelProperty("回调状态：0-未回调；1-已回调")
    private Integer callbackStatus;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    @ApiModelProperty("所属代理ID")
    private Long agentId;

    @ApiModelProperty("所属代理名称")
    private String agentName;

    @ApiModelProperty("商户ID")
    private Long shopId;

    @ApiModelProperty("商户名称")
    private String shopName;

    @ApiModelProperty("码商ID")
    private Long merchantId;

    @ApiModelProperty("码商名称")
    private String merchantName;

    @ApiModelProperty("一级码商名称")
    private String parentMerchantName;

    @ApiModelProperty("账号")
    private String accountNumber;

    @ApiModelProperty("码商Id集合")
    private List<Long> childIds;

    @ApiModelProperty("不包含码商自身")
    private Long selftMerchantId;

    @ApiModelProperty("收银台会员IP")
    private String  memberIp;
}
