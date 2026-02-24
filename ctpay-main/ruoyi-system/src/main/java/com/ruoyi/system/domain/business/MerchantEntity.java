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
 * 码商
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("ct_merchant")
@ApiModel(value = "MerchantEntity对象", description = "码商")
public class MerchantEntity {

    @ApiModelProperty("主键")
    @TableId("user_id")
    private Long userId;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("码商名称")
    private String merchantName;

    @ApiModelProperty("登录密码")
    private String loginPassword;

    @ApiModelProperty("所属代理")
    private Long agentId;

    @ApiModelProperty("所属码商")
    private Long parentId;

    @ApiModelProperty("上级名称")
    private String parentName;

    @ApiModelProperty("码商层级路径")
    private String parentPath;

    @ApiModelProperty("状态：0-正常；1-禁用")
    @TableField("`status`")
    private Integer status;

    @ApiModelProperty("安全码")
    private String safeCode;

    @ApiModelProperty("押金")
    private BigDecimal prepayment;

    @ApiModelProperty("登录白名单")
    private String whiteLoginIp;

    @ApiModelProperty("回调白名单")
    private String whiteCallbackIp;

    @ApiModelProperty("余额")
    private BigDecimal balance;

    @ApiModelProperty("昨日余额")
    private BigDecimal yesBalance;

    @ApiModelProperty("佣金")
    private BigDecimal freezeAmount;

    @ApiModelProperty("TG管理员")
    private String telegram;

    @ApiModelProperty("TG群组")
    private String telegramGroup;

    @ApiModelProperty("开工状态：0-开工；1-未开工")
    private Integer workStatus;

    @ApiModelProperty("接单权限：0-开启；1-关闭")
    private Integer orderPermission;

    @ApiModelProperty("最低接单金额")
    private BigDecimal minAmount;

    @ApiModelProperty("团队接单状态：0-开启；1-关闭")
    private Integer teamStatus;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @ApiModelProperty("删除状态：0-正常；2-删除")
    @TableLogic
    private Integer delFlag;

    @ApiModelProperty("权重")
    private Integer weight;

    @ApiModelProperty("来单提醒:0-开启；1-关闭")
    private Integer orderRemind;

    @ApiModelProperty("码商层级")
    private Integer merchantLevel;

    @ApiModelProperty("是否允许佣金转余额，0-是；1-否")
    private Integer transAmount;

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

    @ApiModelProperty("押金阈值")
    @TableField(exist = false)
    private BigDecimal baseDeposit;

    @TableField(exist = false)
    private Integer currLevel;

    @ApiModelProperty("下级余额汇总")
    @TableField(exist = false)
    private BigDecimal childBalance;

    @ApiModelProperty("下级佣金汇总")
    @TableField(exist = false)
    private BigDecimal childFreezeAmount;

    @TableField(exist = false)
    private BigDecimal totalBalance;

    @ApiModelProperty("是否展示转移金额")
    @TableField(exist = false)
    private Integer showTrimBalance;

    @ApiModelProperty("自动刷新：0-开启；1-关闭")
    private Integer autoRefresh;
}
