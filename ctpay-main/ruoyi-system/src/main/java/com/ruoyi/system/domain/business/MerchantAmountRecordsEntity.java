package com.ruoyi.system.domain.business;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * <p>
 * 
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("ct_merchant_amount_records")
@ApiModel(value = "MerchantAmountRecordsEntity对象", description = "")
public class MerchantAmountRecordsEntity {

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名称")
    private String userName;

    @ApiModelProperty("金额类型：1-预付金额；2-佣金；3-押金")
    private Integer amountType;

    @TableField(exist = false)
    private String amountTypeStr;

    @ApiModelProperty("变更类型：1-系统充值；2-上分;3-佣金转移；4-冲正")
    private Integer changeType;

    @TableField(exist = false)
    private String changeTypeStr;

    @ApiModelProperty("变更前金额")
    private BigDecimal beforeAmount;

    @ApiModelProperty("变更后金额")
    private BigDecimal afterAmount;

    @ApiModelProperty("变更金额")
    private BigDecimal changeAmount;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @TableField(exist = false)
    private String createTimeStr;

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("备注")
    private String notes;

    @ApiModelProperty("描述")
    private String remarks;

    @ApiModelProperty("所属代理")
    private Long agentId;

    @ApiModelProperty("操作人")
    private String operator;
}
