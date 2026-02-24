package com.ruoyi.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.math.BigDecimal;

@ApiModel("佣金")
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class CommissionVO {
    @ApiModelProperty("码商ID")
    private Long merchantId;

    @ApiModelProperty("码商名称")
    private String merchantName;

    @ApiModelProperty("通道ID")
    private Long channelId;

    @ApiModelProperty("通道名称")
    private String channelName;

    @ApiModelProperty("码商层级")
    private Integer merchantLevel;

    @ApiModelProperty("总成功单数")
    private Integer totalCount;

    @ApiModelProperty("总跑量")
    private BigDecimal totalAmount;

    @ApiModelProperty("一级佣金")
    private BigDecimal commissionOne;

    @ApiModelProperty("二级佣金")
    private BigDecimal commissionTwo;

    @ApiModelProperty("三级佣金")
    private BigDecimal commissionThree;

    @ApiModelProperty("四级佣金")
    private BigDecimal commissionFour;

    @ApiModelProperty("我的佣金")
    private BigDecimal myCommission;

    @ApiModelProperty("一级佣金费率")
    private BigDecimal commissionOneRate;

    @ApiModelProperty("二级佣金费率")
    private BigDecimal commissionTwoRate;

    @ApiModelProperty("三级佣金费率")
    private BigDecimal commissionThreeRate;

    @ApiModelProperty("四级佣金费率")
    private BigDecimal commissionFourRate;

    @ApiModelProperty("我的佣金费率")
    private BigDecimal myCommissionRate;

    @ApiModelProperty("点位")
    private BigDecimal dianwei;

    @ApiModelProperty("层级路径")
    private String parentPath;

    @ApiModelProperty("1级码商ID")
    private Long merchantIdOne;

    @ApiModelProperty("1级码商名称")
    private String merchantNameOne;
}
