package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author admin
 * &#064;Date  2024/9/8 2:22
 */
@Getter
@Setter
@ApiModel("通道查询")
public class ChannelQueryDTO extends BasePageDTO{
    @ApiModelProperty("通道名称")
    private String channelName;

    @ApiModelProperty("通道编码")
    private String channelCode;

    @ApiModelProperty("状态：0-正常；1-禁用")
    private Integer status;
}
