package com.ruoyi.system.domain.business;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * <p>
 * 订单表
 * </p>
 *
 * @author admin
 * @since 2024-10-31
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("ct_in_order")
@ApiModel(value = "InOrderEntity对象", description = "订单表")
public class InOrderEntity {

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("商户订单号")
    private String shopOrderNo;

    @ApiModelProperty("系统订单号")
    private String tradeNo;

    @ApiModelProperty("一码通订单号")
    private String onebuyOrderNo;

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

    @ApiModelProperty("码商ID")
    private Long merchantId;

    @ApiModelProperty("码商名称")
    private String merchantName;

    @ApiModelProperty("通道ID")
    private Long channelId;

    @ApiModelProperty("通道名称")
    private String channelName;

    @ApiModelProperty("订单超时时长")
    private Integer overtime;

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

    @ApiModelProperty("订单状态：0-待支付；1-支付成功；2-订单超时；3-已关闭；4-待退回；5-已退回")
    private Integer orderStatus;

    @ApiModelProperty("回调状态：0-未回调；1-已回调")
    private Integer callbackStatus;

    @ApiModelProperty("付款人")
    private String payer;

    @ApiModelProperty("码商是否已增加余额:0-否；1-是")
    private Integer opcoin;

    @ApiModelProperty("随机标识")
    private Integer unionCode;

    @ApiModelProperty("拉单人IP")
    private String clientIp;

    @ApiModelProperty("操作人")
    private String operater;

    @ApiModelProperty
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operaterTime;

    @ApiModelProperty("回执单")
    private String receiptImg;

    @ApiModelProperty("收银台会员IP")
    private String  memberIp;
}
