package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("下级码商通道费率修改")
public class ChildMerchantChannelDTO {
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("码商id")
    private Long merchantId;

    @ApiModelProperty("通道id")
    private Long channelId;

    @ApiModelProperty("通道费率")
    private BigDecimal channelRate;

    @ApiModelProperty("状态:0-开启；1-关闭")
    private Integer status;
}
