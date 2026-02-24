package com.ruoyi.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("码商订单统计")
public class MerchantOrderTotalVO {
    @ApiModelProperty("总订单金额")
    private BigDecimal totalAmount;
    @ApiModelProperty("成功单金额")
    private BigDecimal successAmount;
    @ApiModelProperty("总订单数")
    private Integer totalCount;
    @ApiModelProperty("成功订单数")
    private Integer successCount;
    @ApiModelProperty("成功率")
    private BigDecimal successRate;
}
