
package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("测试单")
public class TestOrderReq {
    @ApiModelProperty("商户ID")
    @NotEmpty(message = "商户ID不能为空")
    private String mchid;
    @ApiModelProperty("下单金额")
    @NotEmpty(message = "下单金额不能为空")
    private String amount;
    @ApiModelProperty("支付通道编码")
    @NotEmpty(message = "支付通道编码不能为空")
    private String channel;
    @ApiModelProperty("回调通知URL")
    private String notify_url;
}
