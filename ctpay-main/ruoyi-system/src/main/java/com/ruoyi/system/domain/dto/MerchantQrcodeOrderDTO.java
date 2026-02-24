package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("码订单请求")
public class MerchantQrcodeOrderDTO extends BasePageDTO{
    @ApiModelProperty("通道ID")
    private Long channelId;

    @ApiModelProperty("码商名称")
    private String merchantName;

    @ApiModelProperty("姓名，昵称")
    private String nickName;

    @ApiModelProperty("账号")
    private String accountNumber;

    @ApiModelProperty("状态（0正常 1停用）")
    private Integer status;

    @ApiModelProperty("删除状态：0-正常；2-删除")
    private Integer delFlag;

    @ApiModelProperty("开工状态（0正常 1停用）")
    private Integer workStatus;

    @ApiModelProperty("接单状态（0正常 1停用）")
    private Integer orderPermission;

    @ApiModelProperty("进单状态（0正常 1停用）")
    private Integer enterOrderStatus;

    private String startTime;

    private String endTime;

    private String startDate;

    private String endDate;

    private Long agentId;
}
