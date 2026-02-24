package com.ruoyi.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.system.domain.business.AgentChannelEntity;
import com.ruoyi.system.domain.business.MerchantQrcodeEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel("收银台查询信息")
public class SytQueryVO {
    @ApiModelProperty("系统订单号")
    private String tradeNo;
    @ApiModelProperty("修正金额")
    private BigDecimal fixedAmount;
    @ApiModelProperty("订单状态")
    private Integer orderStatus;
    @ApiModelProperty("通道名称")
    private String channelName;
    @ApiModelProperty("下单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date orderTime;
    @ApiModelProperty("订单超时时长")
    private Integer overtime;
    @ApiModelProperty("唯一标识")
    private Integer uid;
    @ApiModelProperty("收银台姓名开关：0-开启；1-关闭")
    private Integer memberNameFlag;
    @ApiModelProperty("产品设置")
    private AgentChannelEntity agentChannel;
    @ApiModelProperty("码商单码")
    private MerchantQrcodeEntity merchantQrcode;
}
