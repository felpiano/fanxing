package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("儿子码商")
public class MerchantChildSaveDTO {
    @ApiModelProperty("主键")
    private Long userId;

    @ApiModelProperty("登录密码")
    private String loginPassword;

    @ApiModelProperty("状态：0-正常；1-禁用")
    private Integer status;

    @ApiModelProperty("安全码")
    private String safeCode;

    @ApiModelProperty("开工状态：0-开工；1-未开工")
    private Integer workStatus;

    @ApiModelProperty("接单权限：0-开启；1-关闭")
    private Integer orderPermission;

    @ApiModelProperty("最低接单金额")
    private BigDecimal minAmount;

    @ApiModelProperty("团队接单状态：0-开启；1-关闭")
    private Integer teamStatus;

    @ApiModelProperty("删除状态：0-正常；2-删除")
    private Integer delFlag;

    @ApiModelProperty("登录白名单")
    private String whiteLoginIp;
}
