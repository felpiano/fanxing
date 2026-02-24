package com.ruoyi.system.domain.business;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.math.BigDecimal;

/**
 * <p>
 * 产品
 * </p>
 *
 * @author admin
 * @since 2024-10-21
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("ct_agent_channel")
@ApiModel(value = "AgentChannelEntity对象", description = "产品")
public class AgentChannelEntity {

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("通道ID")
    private Long channelId;

    @ApiModelProperty("代理ID")
    private Long agentId;

    @ApiModelProperty("费率")
    private BigDecimal costRate;

    @ApiModelProperty("状态：0-正常；1-禁用")
    @TableField("`status`")
    private Integer status;

    @ApiModelProperty("订单超时时间（分钟）")
    private Integer overtime;

    @ApiModelProperty("订单金额浮动:0-开启；1-关闭")
    private Integer overAmount;

    @ApiModelProperty("支付页面是否提交姓名：0-开启；1-关闭")
    private Integer payurlUser;

    @ApiModelProperty("初始化页面跳转支付宝：0-开启；1-关闭")
    private Integer initpageForwrod;

    @ApiModelProperty("支付模板")
    private String qrcodeModel;

    @ApiModelProperty("订单浮动金额个数")
    private Integer overAmountValue;

    @ApiModelProperty("订单浮动金额比例0.01*overAmountRate")
    private Integer overAmountRate;

    @ApiModelProperty("订单金额不上浮：0-开启；1-关闭")
    private Integer overAmountDown;

    @ApiModelProperty("自定义引导语")
    private String guidance;

    @ApiModelProperty("码商设置收款码金额区间:0-开启；1-关闭")
    private Integer setAmount;

    @ApiModelProperty("是否锁码：0-开启；1-关闭")
    private Integer lockedCode;

    @ApiModelProperty("隐藏二维码：0-开启；1-关闭")
    private Integer hideQrcode;

    @ApiModelProperty("最大二维码数量")
    private Integer maxCode;

    @ApiModelProperty("是否展示通道：0-开启；1-关闭")
    private Integer showChannel;

    @ApiModelProperty("显示确认收款：0-开启；1-关闭")
    private Integer showConfirm;

    @ApiModelProperty("跳转方式:0-转账，1-扫码")
    private Integer jumpType;

    @ApiModelProperty("传码是否需要账号：0-是；1-否")
    private Integer accountNeed;

    @TableField(exist = false)
    private String channelName;

    @TableField(exist = false)
    private String channelCode;
}
