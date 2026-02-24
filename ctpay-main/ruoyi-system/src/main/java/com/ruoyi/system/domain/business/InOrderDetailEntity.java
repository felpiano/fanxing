package com.ruoyi.system.domain.business;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * <p>
 * 订单副表
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
@TableName("ct_in_order_detail")
@ApiModel(value = "InOrderDetailEntity对象", description = "订单副表")
public class InOrderDetailEntity {

    @ApiModelProperty("订单主键")
    @TableId("order_id")
    private Long orderId;

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

    @ApiModelProperty("回调备注")
    private String callbackRemarks;

    @ApiModelProperty("码商通道ID")
    private Long merchantChannelId;

    @ApiModelProperty("代理产品ID")
    private Long agentChannelId;

    @ApiModelProperty("支付地址")
    private String forwordUrl;

    private Date detailTime;

    private BigDecimal merchantRateOne;

    private BigDecimal merchantRateTwo;

    private BigDecimal merchantRateThree;

    private BigDecimal merchantRateFour;

    private BigDecimal merchantRateFive;
}
