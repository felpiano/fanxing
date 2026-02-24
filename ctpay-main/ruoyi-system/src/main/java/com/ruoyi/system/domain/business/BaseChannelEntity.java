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
 * 基础产品
 * </p>
 *
 * @author admin
 * @since 2024-11-02
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("ct_base_channel")
@ApiModel(value = "BaseChannelEntity对象", description = "基础产品")
public class BaseChannelEntity {

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("通道ID")
    private Long channelId;

    @ApiModelProperty("费率")
    private BigDecimal costRate;

    @ApiModelProperty("状态：0-正常；1-禁用")
    @TableField("`status`")
    private Integer status;

    @ApiModelProperty("订单超时时间（分钟）")
    private Integer overtime;

    @ApiModelProperty("金额浮动：0-开启；1-禁用")
    private Integer overAmount;

    @ApiModelProperty("支付页面是否提交姓名：0-开启；1-关闭")
    private Integer payurlUser;

    @ApiModelProperty("初始化页面跳转支付宝：0-开启；1-关闭")
    private Integer initpageForwrod;

    @ApiModelProperty("支付模板")
    private String qrcodeModel;

    @ApiModelProperty("订单浮动金额个数")
    private Integer overAmountValue;

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

    @ApiModelProperty("是否首次设置产品：0-是；1-否")
    private Integer firstSet;

    @TableField(exist = false)
    private String channelName;

    @TableField(exist = false)
    private String channelCode;
}
