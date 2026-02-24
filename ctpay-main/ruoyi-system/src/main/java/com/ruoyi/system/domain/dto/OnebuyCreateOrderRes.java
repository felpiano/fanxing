package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OnebuyCreateOrderRes {

    @ApiModelProperty(value = "一码通单号")
    private String id;
    @ApiModelProperty(value = "店铺编号")
    private String shop_id;
    @ApiModelProperty(value = "收银台地址")
    private String url;
}
