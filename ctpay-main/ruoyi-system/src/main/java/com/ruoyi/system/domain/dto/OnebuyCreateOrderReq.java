package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class OnebuyCreateOrderReq {

    @ApiModelProperty(value = "系统单号")
    private String order_no;
    @ApiModelProperty(value = "候选店铺列表")
    private List<String> shop;
    @ApiModelProperty(value = "金额（分）")
    private String amount;
}
