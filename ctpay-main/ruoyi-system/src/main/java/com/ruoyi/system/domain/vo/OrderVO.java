package com.ruoyi.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("订单VO")
public class OrderVO {
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("商户订单号")
    private String shopOrderNo;

    @ApiModelProperty("系统订单号")
    private String tradeNo;

    @ApiModelProperty("单码ID")
    private Long qrcodeId;

    @ApiModelProperty("姓名，昵称")
    private String nickName;

    @ApiModelProperty("账号")
    private String accountNumber;

    @ApiModelProperty("uid")
    private String uid;

    @ApiModelProperty("传码方式：0-解析地址；1-直接上传")
    private Integer qrcodeType;

    @ApiModelProperty("二维码解析")
    private String qrcodeValue;

    @ApiModelProperty("二维码地址")
    private String qrcodeUrl;

    @ApiModelProperty("账号备注")
    private String accountRemark;

    private String accountInfo;

    @ApiModelProperty("码商ID")
    private Long merchantId;

    @ApiModelProperty("码商名称")
    private String merchantName;

    @ApiModelProperty("通道ID")
    private Long channelId;

    @ApiModelProperty("通道名称")
    private String channelName;

    @ApiModelProperty("下单金额")
    private BigDecimal orderAmount;

    @ApiModelProperty("支付金额")
    private BigDecimal fixedAmount;

    @ApiModelProperty("下单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date orderTime;

    @ApiModelProperty("完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    @ApiModelProperty("订单状态：0-待支付；1-支付成功；2-订单超时；3-已关闭；4-完成")
    private Integer orderStatus;

    private String orderStatusStr;

    @ApiModelProperty("回调状态：0-未回调；1-已回调")
    private Integer callbackStatus;

    private String callbackStatusStr;

    @ApiModelProperty("付款人")
    private String payer;

    @ApiModelProperty("是否已增加余额:0-否；1-是")
    private Integer opcoin;

    @ApiModelProperty("随机标识")
    private Integer unionCode;

    @ApiModelProperty("所属代理ID")
    private Long agentId;

    @ApiModelProperty("所属代理名称")
    private String agentName;

    @ApiModelProperty("商户ID")
    private Long shopId;

    @ApiModelProperty("商户名称")
    private String shopName;

    @ApiModelProperty("码商费率")
    private BigDecimal merchantRate;

    @ApiModelProperty("码商手续费")
    private BigDecimal merchantFee;

    @ApiModelProperty("商户费率")
    private BigDecimal shopRate;

    @ApiModelProperty("商户手续费")
    private BigDecimal shopFee;

    @ApiModelProperty("代理费率")
    private BigDecimal agentRate;

    @ApiModelProperty("代理手续费")
    private BigDecimal agentFee;

    @ApiModelProperty("通道编码")
    private String channelCode;

    @ApiModelProperty("回调商户地址")
    private String callShopUrl;

    @ApiModelProperty("订单备注")
    private String orderRemark;

    @ApiModelProperty("路径")
    private String parentPath;

    @ApiModelProperty("一级码商ID")
    private Long firstUserId;

    @ApiModelProperty("一级码商名称")
    private String firstUserName;

    @ApiModelProperty("拉单人IP")
    private String clientIp;

    @ApiModelProperty("操作人")
    private String operater;

    @ApiModelProperty
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operaterTime;

    @ApiModelProperty("我的层级")
    private Integer myMerchantLevel;

    @ApiModelProperty("码商层级")
    private Integer merchantLevel;

    @ApiModelProperty("一级码商费率")
    private BigDecimal merchantRateOne;

    @ApiModelProperty("二级码商费率")
    private BigDecimal merchantRateTwo;

    @ApiModelProperty("三级码商费率")
    private BigDecimal merchantRateThree;

    @ApiModelProperty("四级码商费率")
    private BigDecimal merchantRateFour;

    @ApiModelProperty("五级码商费率")
    private BigDecimal merchantRateFive;

    @ApiModelProperty("是否展示拉黑IP按钮,已拉黑的Ip，返回1，否则返回0")
    private Integer showPusBlack;

    @ApiModelProperty("1级码商佣金，给代理看")
    private BigDecimal firstMerchantFee;

    @ApiModelProperty("我的佣金")
    private BigDecimal myFee;

    @ApiModelProperty("下级佣金")
    private BigDecimal childFee;

    @ApiModelProperty("回执单")
    private String receiptImg;

    @ApiModelProperty("收银台会员IP")
    private String  memberIp;

    @ApiModelProperty("码商余额")
    private String merchantBalance;

    @ApiModelProperty("码商冻结金额")
    private String merchantFreeze;
}
