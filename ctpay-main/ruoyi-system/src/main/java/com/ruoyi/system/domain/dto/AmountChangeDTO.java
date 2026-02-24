package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("商户余额操作")
public class AmountChangeDTO {
    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名称")
    private String userName;

    @ApiModelProperty("变更金额：正数为增加；负数为减少")
    private BigDecimal changeAmount;

    @ApiModelProperty("金额类型：1-余额；2-冻结金额；3-押金")
    private Integer amountType = 1;

    @ApiModelProperty("变更类型：1-系统充值；2-上分;3-佣金转移；4-冲正")
    private Integer changeType = 1;

    @ApiModelProperty("描述")
    private String remarks;

    @ApiModelProperty("备注")
    private String notes;

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("所属代理,商户和码商需要该字段")
    private Long agentId;


    private Long type;

    @ApiModelProperty("操作人")
    private String operator;
}
