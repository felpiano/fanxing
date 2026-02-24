package com.ruoyi.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("上级码商信息")
public class ParentMerchantVO {
    private String userId;

    private String userName;

    private String merchantLevel;
}
