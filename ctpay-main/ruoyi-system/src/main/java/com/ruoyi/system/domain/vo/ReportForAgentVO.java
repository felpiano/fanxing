package com.ruoyi.system.domain.vo;

import com.ruoyi.common.utils.SecurityUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;


import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("代理对账金额")
public class ReportForAgentVO {
    @ApiModelProperty("码商ID")
    private Long userId;
    @ApiModelProperty("码商名称")
    private String userName;
    @ApiModelProperty("通道ID")
    private Long channelId;
    @ApiModelProperty("通道名称")
    private String channelName;
    @ApiModelProperty("一级码商ID")
    private Long firstUserId;
    @ApiModelProperty("一级码商名称")
    private String firstUserName;
    @ApiModelProperty("商户ID")
    private Long shopId;
    @ApiModelProperty("商户名称")
    private String shopName;
    @ApiModelProperty("总条数")
    private Integer totalCount;
    @ApiModelProperty("成功条数")
    private Integer successCount;
    @ApiModelProperty("总交易金额")
    private BigDecimal totalMoney;
    @ApiModelProperty("成功金额")
    private BigDecimal successMoney;
    @ApiModelProperty("昨日余额")
    private BigDecimal yesBalance;
    @ApiModelProperty("昨日佣金")
    private BigDecimal yesFreeze;
    @ApiModelProperty("今日余额")
    private BigDecimal todayBalance;
    @ApiModelProperty("今日佣金")
    private BigDecimal todayFreeze;
    @ApiModelProperty("佣金")
    private BigDecimal merchantFee;
    @ApiModelProperty("今日上分")
    private BigDecimal chargeMoney;
    @ApiModelProperty("今日转移")
    private BigDecimal chargeToMoney;
    @ApiModelProperty("成功率")
    private BigDecimal successRate;

    private Integer parentId;
    private String parentPath;
    private Integer merchantLevel;

}
