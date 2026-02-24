package com.ruoyi.system.domain.business;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 代理基础信息表
 * </p>
 *
 * @author admin
 * @since 2024-09-08
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ct_agent")
@ApiModel(value = "代理基础信息表", description = "代理基础信息表")
public class AgentEntity {

    @ApiModelProperty("用户ID")
    @TableId("user_id")
    private Long userId;

    @ApiModelProperty("用户登录名")
    private String userName;

    @ApiModelProperty("用户昵称")
    private String nickName;

    @ApiModelProperty("帐号状态（0正常 1停用）")
    @TableField("`status`")
    private Integer status;

    @ApiModelProperty
    private BigDecimal balance;

    @ApiModelProperty("删除标志（0代表存在 2代表删除）")
    @TableLogic
    private Integer delFlag;

    @ApiModelProperty("轮训类型：0-二维码；1-码商通道")
    private Integer weightType;

    @ApiModelProperty("佣金划转开关：0-开启；1-关闭")
    private Integer commissionType;

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

    @TableField(exist = false)
    private String uid;

    @TableField(exist = false)
    private String password;
}
