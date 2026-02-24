package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@ApiModel("新增通道")
@Data
public class ChannelProductDTO {
    @ApiModelProperty("通道名称")
    private String channelName;

    @ApiModelProperty("通道编码")
    private String channelCode;

    @ApiModelProperty("状态：0-正常；1-禁用")
    private Integer status;

    @ApiModelProperty("删除状态：0-正常；2-删除")
    private Integer delFlag;

    @ApiModelProperty("提示信息")
    private String notes;


    @ApiModelProperty("费率")
    private BigDecimal costRate;

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
}
