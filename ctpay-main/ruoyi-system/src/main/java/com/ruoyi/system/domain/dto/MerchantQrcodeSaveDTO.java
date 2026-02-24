package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@ApiModel("码商上码保存")
public class MerchantQrcodeSaveDTO {

    @ApiModelProperty("主键，新增时为空")
    private Long id;

    @ApiModelProperty("码商id")
    @NotNull(message = "码商id不允许为空")
    private Long merchantId;

    @ApiModelProperty("码商通道id")
    @NotNull(message = "通道id不允许为空")
    private Long channelId;

    @ApiModelProperty("姓名，昵称")
    private String nickName;

    @ApiModelProperty("账号")
    private String accountNumber;

    @ApiModelProperty("uid")
    private String uid;

    @ApiModelProperty("传码方式：0-解析地址；1-直接上传；2-无码")
    @NotNull(message = "传码方式不允许为空")
    private Integer qrcodeType;

    @ApiModelProperty("二维码解析")
    private String qrcodeValue;

    @ApiModelProperty("二维码地址")
    private String qrcodeUrl;

    @ApiModelProperty("账号备注")
    private String accountRemark;

    @ApiModelProperty("最小金额")
    private BigDecimal minAmount;

    @ApiModelProperty("最大金额")
    private BigDecimal maxAmount;

    @ApiModelProperty("日限额")
    private BigDecimal dayLimit;

    @ApiModelProperty("笔数上限")
    private Integer countLimit;

    @ApiModelProperty("安全码")
    @NotEmpty(message = "安全码不允许为空")
    private String safeCode;
}
