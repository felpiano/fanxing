package com.ruoyi.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("码统计")
public class MerchantQrcodeReportVO {
    @ApiModelProperty("支付宝账号总数")
    private Integer totalCount;

    @ApiModelProperty("可用账号")
    private Integer openCount;

    @ApiModelProperty("可接单码商数")
    private Integer acceptCount;

    @ApiModelProperty("可用二维码总数")
    private Integer useCount;
}
