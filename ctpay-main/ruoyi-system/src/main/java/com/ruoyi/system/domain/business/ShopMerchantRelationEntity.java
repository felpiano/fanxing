package com.ruoyi.system.domain.business;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * <p>
 * 商户与码商关系表
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
@TableName("ct_shop_merchant_relation")
@ApiModel(value = "ShopMerchantRelationEntity对象", description = "商户与码商关系表")
public class ShopMerchantRelationEntity {

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("商户id")
    private Long shopId;

    @ApiModelProperty("码商id")
    private Long merchantId;

    @ApiModelProperty("状态：0-正常；1-禁用")
    @TableField("`status`")
    private Integer status;

    @ApiModelProperty("码商名称")
    @TableField(exist = false)
    private String merchantName;

    @ApiModelProperty("码商层级")
    @TableField(exist = false)
    private Integer merchantLevel;
}
