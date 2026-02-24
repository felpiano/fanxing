package com.ruoyi.system.domain.business;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * <p>
 * 码商通道费率
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("ct_merchant_channel")
@ApiModel(value = "MerchantChannelEntity对象", description = "码商通道费率")
public class MerchantChannelEntity {

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("码商id")
    private Long merchantId;

    @ApiModelProperty("通道id")
    private Long channelId;

    @ApiModelProperty("通道费率")
    private BigDecimal channelRate;

    @ApiModelProperty("状态:0-开启；1-关闭")
    @TableField("`status`")
    private Integer status;

    @ApiModelProperty("最小金额")
    private BigDecimal minAmount;

    @ApiModelProperty("最大金额")
    private BigDecimal maxAmount;

    @ApiModelProperty("是否需要安全码：0-是；1-否")
    private Integer needSafecode;

    @ApiModelProperty("订单超时时长（分钟）")
    private Integer merchantOvertime;

    @ApiModelProperty("押金阈值")
    private BigDecimal baseDeposit;

    @ApiModelProperty("权重")
    private Integer weight;

    @ApiModelProperty("代理控制进单权限:0-正常；1-关闭")
    private Integer agentContrl;

    private BigDecimal merchantRateOne;

    private BigDecimal merchantRateTwo;

    private BigDecimal merchantRateThree;

    private BigDecimal merchantRateFour;

    private BigDecimal merchantRateFive;

    @TableField(exist = false)
    private String channelName;

    @TableField(exist = false)
    private String channelCode;
}
