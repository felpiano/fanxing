package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("订单统计查询")
public class MerchantOrderReportDTO {
    @ApiModelProperty("码商ID")
    private Long merchantId;
    @ApiModelProperty("所属代理ID")
    private Long agentId;
    @ApiModelProperty("商户ID")
    private Long shopId;
    @ApiModelProperty("开始时间")
    private String startTime;
    @ApiModelProperty("结束时间")
    private String endTime;
}
