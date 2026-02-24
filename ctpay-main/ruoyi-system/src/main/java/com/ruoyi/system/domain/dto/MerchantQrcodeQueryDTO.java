package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("码商上码查询")
public class MerchantQrcodeQueryDTO extends BasePageDTO{
    @ApiModelProperty("码商ID")
    private Long merchantId;

    @ApiModelProperty("通道id")
    private Long channelId;

    @ApiModelProperty("昵称，姓名")
    private String nickName;

    @ApiModelProperty("账号")
    private String accountNumber;

    @ApiModelProperty("账号")
    private String accountRemark;

    @ApiModelProperty("帐号状态（0正常 1停用）")
    private Integer status;

    @ApiModelProperty("码商名称")
    private String merchantName;

    @ApiModelProperty("父亲路径")
    private String parentPath;
}
