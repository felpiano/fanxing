package com.ruoyi.system.domain.business;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * <p>
 * 商户
 * </p>
 *
 * @author admin
 * @since 2024-10-18
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("ct_shop")
@ApiModel(value = "AgentShopEntity对象", description = "商户")
public class ShopEntity {

    @ApiModelProperty("商户ID")
    @TableId("user_id")
    private Long userId;

    @ApiModelProperty("登录账号")
    private String userName;

    @ApiModelProperty("登录密码")
    @TableField(exist = false)
    private String loginPassword;

    @ApiModelProperty("所属代理")
    private Long agentId;

    @ApiModelProperty("代理登录名")
    private String agentName;

    @ApiModelProperty("商户名称")
    private String shopName;

    @ApiModelProperty("Telegram账号")
    private String telegram;

    @ApiModelProperty("telegram群组")
    private String telegramGroup;

    @ApiModelProperty("安全码")
    private String safeCode;

    @ApiModelProperty("代付备注")
    private String proxyPaymentNotes;

    @ApiModelProperty("是否允许代付：0-是；1-否")
    private Integer proxyPaymentFlag;

    @ApiModelProperty("是否验证：0-是；1-否")
    private Integer verificationFlag;

    @ApiModelProperty("提现手续费")
    private BigDecimal wthdrawalFee;

    @ApiModelProperty("帐号状态（0正常 1停用）")
    @TableField("`status`")
    private Integer status;

    @ApiModelProperty("商户余额")
    private BigDecimal balance;

    @ApiModelProperty("创建人")
    private Long createBy;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @ApiModelProperty("更新人")
    private Long updateBy;

    @ApiModelProperty("删除状态：0-正常；2-删除")
    @TableLogic
    private Integer delFlag;

    @ApiModelProperty("进单状态：0开启、1关闭")
    private Integer enterOrder;

    @ApiModelProperty("最小金额")
    private BigDecimal minAmount;

    @ApiModelProperty("最大金额")
    private BigDecimal maxAmount;

    @ApiModelProperty("密钥")
    private String signSecret;

    @ApiModelProperty("收银台姓名开关：0-开启；1-关闭")
    private Integer memberNameFlag;

    @TableField(exist = false)
    @ApiModelProperty("指定码商，多个以英文逗号分隔")
    private String merchantIds;

    @ApiModelProperty("最后登录ID")
    @TableField(exist = false)
    private String lastLoginIp;

    @ApiModelProperty("最后登录时间")
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    @ApiModelProperty("谷歌开关")
    @TableField(exist = false)
    private Integer googleSecretFlag;

    @ApiModelProperty("谷歌密钥")
    @TableField(exist = false)
    private String googleSecret;

    @ApiModelProperty("登录白名单")
    @TableField(exist = false)
    private String allowLoginIp;

    @ApiModelProperty("下单地址")
    @TableField(exist = false)
    private String submitOrderUrl;

    @ApiModelProperty("回调IP")
    @TableField(exist = false)
    private String clientIp;
}
