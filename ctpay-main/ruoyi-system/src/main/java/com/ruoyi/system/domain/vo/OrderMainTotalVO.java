package com.ruoyi.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@ApiModel("订单统计")
@Data
public class OrderMainTotalVO {
    @ApiModelProperty("订单数")
    private Integer orderNum;

    @ApiModelProperty("下单金额")
    private BigDecimal totalAmount;

    @ApiModelProperty("成功单数")
    private Integer successCount;

    @ApiModelProperty("成功金额")
    private BigDecimal successAmount;

    @ApiModelProperty("结算金额")
    private BigDecimal realAmount;

    @ApiModelProperty("手续费")
    private BigDecimal serviceFee;

    @ApiModelProperty("成本")
    private BigDecimal costFee;

    @ApiModelProperty("日期")
    private String dt;

    @ApiModelProperty("成交率")
    private BigDecimal successRate;
}
