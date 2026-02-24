package com.ruoyi.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopChargeRecordsExportVo {

    @ApiModelProperty("商户名称")
    private String shopName;

    @ApiModelProperty("钱包类型：1资金钱包2预付钱包")
    private String purseType;

    @ApiModelProperty("变更类型：1充值2下发3转移4调账")
    private String changeType;

    @ApiModelProperty("变更前余额")
    private String changeBefore;

    @ApiModelProperty("变更金额")
    private String changeAmount;

    @ApiModelProperty("变更后余额")
    private String changeAfter;

    @ApiModelProperty("变更时间")
    private String changeTime;

    @ApiModelProperty("变更信息")
    private String notes;

    @ApiModelProperty("商户订单号")
    private String orderNo;

    @ApiModelProperty("系统订单号")
    private String traceNo;
}
