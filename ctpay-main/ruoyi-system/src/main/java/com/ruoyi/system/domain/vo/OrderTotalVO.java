package com.ruoyi.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@ApiModel("订单汇总")
@Data
public class OrderTotalVO {

    @ApiModelProperty("成功金额")
    private BigDecimal totalAmount;

    @ApiModelProperty("成功笔数")
    private Long totalCount;
}
