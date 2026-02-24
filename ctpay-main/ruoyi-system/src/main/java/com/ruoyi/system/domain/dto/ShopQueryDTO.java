package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author admin
 * &#064;Date  2024/9/13 23:20
 */
@Getter
@Setter
@ApiModel("商户查询")
public class ShopQueryDTO extends BasePageDTO{
    @ApiModelProperty("商户ID")
    private Long userId;

    @ApiModelProperty("登录账号")
    private String userName;

    @ApiModelProperty("代理登录名")
    private String agentName;

    @ApiModelProperty("商户名称")
    private String shopName;

    @ApiModelProperty("Telegram账号")
    private String telegram;

    @ApiModelProperty("帐号状态（0正常 1停用）")
    private Integer status;

    @ApiModelProperty("删除状态：0-正常；2-删除")
    private Integer delFlag;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;
}
