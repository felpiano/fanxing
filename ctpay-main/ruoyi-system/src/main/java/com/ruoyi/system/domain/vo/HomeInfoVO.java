package com.ruoyi.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author admin
 * @Date 2024/9/19 22:59
 */
@Data
@ApiModel("数量统计")
public class HomeInfoVO {
    @ApiModelProperty("商户数")
    private Integer shopCount;
    @ApiModelProperty("通道数")
    private Integer channelCount;
    @ApiModelProperty("产品数")
    private Integer productCount;
    @ApiModelProperty("代理数")
    private Integer agentCount;
}
