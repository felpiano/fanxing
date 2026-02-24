package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel("查询类型")
public class AmountChangeQueryDTO extends BasePageDTO{
    @ApiModelProperty("id")
    private Long userId;

    @ApiModelProperty("名称")
    private String userName;

    @ApiModelProperty("金额类型：1-余额；2-冻结金额；3-预付金额")
    private Integer amountType;

    @ApiModelProperty("变更类型：1-充值；2-上分；3-转移；4-冲正；5-扣款")
    private Integer changeType;

    @ApiModelProperty("最大变更金额范围")
    private BigDecimal maxAmount;

    @ApiModelProperty("最小变更金额范围")
    private BigDecimal minAmount;

    @ApiModelProperty("变更开始时间")
    private String startTime;

    @ApiModelProperty("变更结束时间")
    private String endTime;

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("码商层级")
    private String merchantLevel;

    private String parentPath;

    private List<Long> parentIds;
}
