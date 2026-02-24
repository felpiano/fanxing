package com.ruoyi.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("订单报表统计")
public class OrderReportVO {
//    @ApiModelProperty("id")
//    private String id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("交易笔数")
    private Long totalCount;
    @ApiModelProperty("交易总额")
    private BigDecimal totalAmount;
    @ApiModelProperty("成功订单数")
    private Long realCount;
    @ApiModelProperty("实付总额")
    private BigDecimal realFaceValue;
    @ApiModelProperty("手续费")
    private BigDecimal shopRateFee;
    @ApiModelProperty("核销成本")
    private BigDecimal platRateFee;
    @ApiModelProperty("成功率")
    private String successRate;
    @ApiModelProperty("本期留存")
    private BigDecimal currFee;
}
