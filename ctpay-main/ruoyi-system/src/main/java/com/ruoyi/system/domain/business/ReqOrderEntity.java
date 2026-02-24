package com.ruoyi.system.domain.business;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * <p>
 * 订单请求
 * </p>
 *
 * @author admin
 * @since 2024-11-18
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("ct_req_order")
@ApiModel(value = "ReqOrderEntity对象", description = "订单请求")
public class ReqOrderEntity {

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("商户ID")
    private String mchid;

    @ApiModelProperty("商户订单号")
    private String outTradeNo;

    @ApiModelProperty("下单金额")
    private String amount;

    @ApiModelProperty("支付通道编码")
    @TableField("`channel`")
    private String channel;

    @ApiModelProperty("回调通知URL")
    private String notifyUrl;

    @ApiModelProperty("同步回调地址")
    private String returnUrl;

    @ApiModelProperty("时间")
    private String timeStamp;

    @ApiModelProperty("宝转卡，必须传支付宝用户名，否则无法到账其它请填123")
    private String body;

    @ApiModelProperty("签名")
    private String sign;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
