package com.ruoyi.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("接单匹配视图")
public class SuitableQrcodeVO {

    @ApiModelProperty("单码ID")
    private Long qrcodeId;

    @ApiModelProperty("码商ID")
    private Long merchantId;

    @ApiModelProperty("码商名称")
    private String merchantName;

    @ApiModelProperty("码商余额")
    private String balance;

    @ApiModelProperty("所属代理ID")
    private Long agentId;

    @ApiModelProperty("所属代理名称")
    private String agentName;

    @ApiModelProperty("代理费率")
    private BigDecimal agentRate;

    @ApiModelProperty("单码用户名或昵称")
    private String nickName;

    @ApiModelProperty("单码账号")
    private String accountNumber;

    @ApiModelProperty("单码UID")
    private String uid;

    @ApiModelProperty("单码账号备注")
    private String accountRemark;

    @ApiModelProperty("单码最小接单金额")
    private BigDecimal qrcodeMinAmount;

    @ApiModelProperty("单码最大接单金额")
    private BigDecimal qrcodeMaxAmount;

    @ApiModelProperty("单码传码方式")
    private Integer qrcodeType;

    @ApiModelProperty("单码二维码解析")
    private String qrcodeValue;

    @ApiModelProperty("单码二维码图片地址")
    private String qrcodeUrl;

    @ApiModelProperty("单码日限额")
    private BigDecimal dayLimit;

    @ApiModelProperty("单码笔数限制")
    private Integer countLimit;

    @ApiModelProperty("码商费率")
    private BigDecimal merchantRate;

    @ApiModelProperty("码商最小金额")
    private BigDecimal merchantMinAmount;

    @ApiModelProperty("码商最大金额")
    private BigDecimal merchantMaxAmount;

    @ApiModelProperty("商户ID")
    private Long shopId;

    @ApiModelProperty("商户名称")
    private String shopName;

    @ApiModelProperty("商户密钥")
    private String signSecret;

    @ApiModelProperty("商户最小金额")
    private BigDecimal shopMinAmount;

    @ApiModelProperty("商户最大金额")
    private BigDecimal shopMaxAmount;

    @ApiModelProperty("商户费率")
    private BigDecimal shopRate;

    @ApiModelProperty("通道ID")
    private Long channelId;

    @ApiModelProperty("通道编码")
    private String channelCode;

    @ApiModelProperty("通道名称")
    private String channelName;

    @ApiModelProperty("通道超时时长（分钟）")
    private Long overtime;

    @ApiModelProperty("码商订单超时时长（分钟）")
    private Long merchantOvertime;

    @ApiModelProperty("权重")
    private Integer weight;

    @ApiModelProperty("浮动金额")
    private Integer overAmount;

    @ApiModelProperty("是否锁码：0-开启；1-关闭")
    private Integer lockedCode;

    @ApiModelProperty("码商设置金额区间")
    private Integer setAmount;

    @ApiModelProperty("订单浮动金额个数")
    private Integer overAmountValue;

    @ApiModelProperty("订单浮动金额比例0.01*overAmountRate")
    private Integer overAmountRate;

    @ApiModelProperty("码商通道ID")
    private Long merchantChannelId;

    @ApiModelProperty("代理产品ID")
    private Long agentChannelId;

    @ApiModelProperty("回调通知URL")
    private String notify_url;

    @ApiModelProperty("支付模板")
    private String qrcodeModel;

    @ApiModelProperty("支付地址")
    private String forwordUrl;

    @ApiModelProperty("最小接单金额")
    private BigDecimal minMerchantGetAmount;

    @ApiModelProperty("来单提醒：0-开启；1-关闭")
    private Integer orderRemind;

    @ApiModelProperty("押金阈值")
    private BigDecimal baseDeposit;

    @ApiModelProperty("轮训类型：0-二维码；1-码商通道")
    private Integer weightType;

    private BigDecimal merchantRateOne;

    private BigDecimal merchantRateTwo;

    private BigDecimal merchantRateThree;

    private BigDecimal merchantRateFour;

    private BigDecimal merchantRateFive;

    private BigDecimal fixAmount;
}
