package com.ruoyi.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @author admin
 * @Date 2024/9/17 0:53
 */
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("商户返回实体")
public class ShopOrderVO {
    @ApiModelProperty("商户ID")
    private String mid;
    @ApiModelProperty("商户单号")
    private String order_no;
    @ApiModelProperty("系统单号")
    private String trace_no;
    @ApiModelProperty("下单金额")
    private String amount;
    @ApiModelProperty("下单时间")
    private String order_time;
    @ApiModelProperty("支付通道编码")
    private String pay_type;
    @ApiModelProperty("回调通知URL")
    private String notify_url;
    @ApiModelProperty("收银台URL")
    private String forward_url;
}
