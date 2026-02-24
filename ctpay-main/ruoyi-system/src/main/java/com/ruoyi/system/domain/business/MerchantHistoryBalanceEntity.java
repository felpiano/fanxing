package com.ruoyi.system.domain.business;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 码商每日余额历史
 * </p>
 *
 * @author admin
 * @since 2024-11-06
 */
@Getter
@Setter
@TableName("ct_merchant_history_balance")
@ApiModel(value = "MerchantHistoryBalanceEntity对象", description = "码商每日余额历史")
public class MerchantHistoryBalanceEntity {

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户账号")
    private String userName;

    @ApiModelProperty("日期")
    private String dataDt;

    @ApiModelProperty("余额")
    private BigDecimal balance;

    @ApiModelProperty("佣金")
    private BigDecimal freezeAmount;
}
