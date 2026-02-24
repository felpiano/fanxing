package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("码商查询类")
public class MerchantQueryDTO extends BasePageDTO{
    @ApiModelProperty("用户id")
    private String userId;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("上级名称")
    private String parentName;

    @ApiModelProperty("开工状态：0-开工；1-未开工")
    private Integer workStatus;

    @ApiModelProperty("接单权限：0-开启；1-关闭")
    private Integer orderPermission;

    @ApiModelProperty("层级查询")
    private String parentPath;

    @ApiModelProperty("层级")
    private String merchantLevel;

    @ApiModelProperty("父亲ID")
    private Long parentId;

    @ApiModelProperty("通道ID")
    private Long channelId;
}
