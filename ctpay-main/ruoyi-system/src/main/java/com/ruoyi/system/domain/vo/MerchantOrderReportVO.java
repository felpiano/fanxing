package com.ruoyi.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("代理订单统计")
public class MerchantOrderReportVO {
    @ApiModelProperty("码ID")
    private Long qrcodeId;
    @ApiModelProperty("码商余额")
    private BigDecimal merchantBalance;
    @ApiModelProperty("码商下级余额")
    private BigDecimal childMerchantBalance;

    @ApiModelProperty("代理看板总提成")
    private BigDecimal agentFee;

    @ApiModelProperty("今日总交易金额")
    private BigDecimal totalAmount;
    @ApiModelProperty("今日总订单笔数")
    private Integer totalCount;
    @ApiModelProperty("今日总提成")
    private BigDecimal merchantFee;
    @ApiModelProperty("今日下级总跑量")
    private BigDecimal childAmount;
    @ApiModelProperty("今日下级订单笔数")
    private Integer childTotalCount;
    @ApiModelProperty("昨日余额")
    private BigDecimal yesBalance;

    @ApiModelProperty("昨日总交易金额")
    private BigDecimal yesTotalAmount;
    @ApiModelProperty("昨日总订单笔数")
    private Integer yesTotalCount;
    @ApiModelProperty("昨日下级订单笔数")
    private Integer yesChildTotalCount;
    @ApiModelProperty("码商昨日总提成金额")
    private BigDecimal yesMerchantFee;
    @ApiModelProperty("昨日下级总跑量")
    private BigDecimal yesChildAmount;
    @ApiModelProperty("前日余额")
    private BigDecimal beforeYesBalance;
}
