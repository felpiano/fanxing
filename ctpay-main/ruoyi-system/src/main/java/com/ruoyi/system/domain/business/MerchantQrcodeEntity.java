package com.ruoyi.system.domain.business;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * <p>
 * 码商上码
 * </p>
 *
 * @author admin
 * @since 2024-10-20
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("ct_merchant_qrcode")
@ApiModel(value = "MerchantQrcodeEntity对象", description = "码商上码")
public class MerchantQrcodeEntity {

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("码商通道id")
    private Long merchantChannelId;

    @ApiModelProperty("码商id")
    private Long merchantId;

    @ApiModelProperty("代理id")
    private Long agentId;

    @ApiModelProperty("通道id")
    private Long channelId;

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

    @ApiModelProperty("最小金额")
    private BigDecimal minAmount;

    @ApiModelProperty("最大金额")
    private BigDecimal maxAmount;

    @ApiModelProperty("日限额")
    private BigDecimal dayLimit;

    @ApiModelProperty("笔数上限")
    private Integer countLimit;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty("删除状态：0-正常；2-删除")
    @TableLogic
    private Integer delFlag;

    @ApiModelProperty("帐号状态（0正常 1停用）")
    @TableField("`status`")
    private Integer status;

    @ApiModelProperty("成功金额")
    @TableField(exist = false)
    private BigDecimal successAmount;

    @ApiModelProperty("成功条数")
    @TableField(exist = false)
    private Integer successCount;

    @ApiModelProperty("昨日成功金额")
    private BigDecimal yesSuccessAmount;

    @ApiModelProperty("昨日成功条数")
    private Integer yesSuccessCount;

    @ApiModelProperty("通道编码")
    @TableField(exist = false)
    private String channelCode;

    @ApiModelProperty("通道名称")
    @TableField(exist = false)
    private String channelName;

    @ApiModelProperty("码商名称")
    @TableField(exist = false)
    private String merchantName;

    @ApiModelProperty("条数")
    @TableField(exist = false)
    private Integer qrcodeNum;
}
