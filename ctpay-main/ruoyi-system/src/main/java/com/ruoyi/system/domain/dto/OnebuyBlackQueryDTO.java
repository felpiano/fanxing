package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author admin
 * &#064;Date  2024/9/8 2:22
 */
@Getter
@Setter
@ApiModel("黑名单用户标识查询")
public class OnebuyBlackQueryDTO extends BasePageDTO{
    @ApiModelProperty("黑名单用户标识")
    private String payer;
}
