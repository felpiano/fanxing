package com.ruoyi.system.domain.business;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * <p>
 * 商户通道费率
 * </p>
 *
 * @author admin
 * @since 2024-10-18
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("ct_shop_base_channel")
@ApiModel(value = "ShopBaseChannelEntity对象", description = "商户通道费率")
public class ShopBaseChannelEntity {

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("商户id")
    private Long shopId;

    @ApiModelProperty("通道id")
    private Long channelId;

    @ApiModelProperty("通道费率")
    private BigDecimal channelRate;

    @ApiModelProperty("支付开关：0-开启；1-关闭")
    private Integer status;

    @TableField(exist = false)
    private String channelCode;

    @TableField(exist = false)
    private String channelName;
}
