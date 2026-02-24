package com.ruoyi.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author admin
 * @Date 2024/9/19 23:12
 */
@ApiModel("成交率排名")
@Data
public class HomeSortVO {
    @ApiModelProperty("名称")
    private String key;
    @ApiModelProperty("值")
    private String value;
    @ApiModelProperty("类型：1通道成交率；2通道交易量；3商户交易量")
    private String type;
}
