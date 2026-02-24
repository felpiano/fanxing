package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("商户码商关联关系查询")
public class ShopMerchantRelationQueryDTO  extends BasePageDTO{
    @ApiModelProperty("商户ID")
    private Long shopId;

    @ApiModelProperty("码商名称")
    private String merchantName;
}
