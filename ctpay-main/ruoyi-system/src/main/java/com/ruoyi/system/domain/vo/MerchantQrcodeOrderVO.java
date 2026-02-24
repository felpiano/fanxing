package com.ruoyi.system.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("根据码统计订单")
public class MerchantQrcodeOrderVO {

    @ApiModelProperty("姓名，昵称")
    private String nickName;

    @ApiModelProperty("账号")
    private String accountNumber;

    @ApiModelProperty("uid")
    private String uid;

    @ApiModelProperty("传码方式：0-解析地址；1-直接上传")
    private Integer qrcodeType;

    @ApiModelProperty("二维码解析")
    private String qrcodeValue;

    @ApiModelProperty("二维码地址")
    private String qrcodeUrl;

    @ApiModelProperty("账号备注")
    private String accountRemark;

    @ApiModelProperty("码商ID")
    private Long merchantId;

    @ApiModelProperty("码商名称")
    private String merchantName;

    @ApiModelProperty("通道ID")
    private Long channelId;

    @ApiModelProperty("通道名称")
    private String channelName;

    @ApiModelProperty("最小金额")
    private BigDecimal minAmount;

    @ApiModelProperty("最大金额")
    private BigDecimal maxAmount;

    @ApiModelProperty("日限额")
    private BigDecimal dayLimit;

    @ApiModelProperty("账号状态（0正常 1停用）")
    private Integer status;

    @ApiModelProperty("删除状态：0-正常；2-删除")
    private Integer delFlag;

    @ApiModelProperty("开工状态（0正常 1停用）")
    private Integer workStatus;

    @ApiModelProperty("接单状态（0正常 1停用）")
    private Integer orderPermission;

    @ApiModelProperty("进单状态（0正常 1停用）")
    private Integer enterOrderStatus;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty("成功金额")
    private BigDecimal successAmount;

    @ApiModelProperty("成功条数")
    private Integer successCount;

    @ApiModelProperty("总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty("总条数")
    private Integer totalCount;

    @ApiModelProperty("成功率")
    private BigDecimal successRate;

}
