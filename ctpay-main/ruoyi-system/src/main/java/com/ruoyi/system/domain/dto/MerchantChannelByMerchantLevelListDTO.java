package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("下级码商通道费率修改")
public class MerchantChannelByMerchantLevelListDTO {

    @ApiModelProperty("码商层级")
    private Integer merchantLevel;


    private ChildMerchantChannelDTO childMerchantChannelDTO;

}
