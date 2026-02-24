package com.ruoyi.system.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author admin
 * @Date 2024/9/10 21:59
 */
@ApiModel("模板通道")
@Data
public class ModelChannelVO {
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("第三方平台ID")
    private Long platId;

    @ApiModelProperty("通道ID")
    private Long channelId;

    @ApiModelProperty("通道名称")
    private String channelName;

    @ApiModelProperty("通道编码")
    private String channelCode;

    @ApiModelProperty("平台编码")
    private String mappingCode;

    @ApiModelProperty("平台费率")
    private BigDecimal costFee;

    @ApiModelProperty("超时时间（秒）")
    private Long timeoutTime;

    @ApiModelProperty("状态：0-正常；1-禁用")
    @TableField("`status`")
    private Integer status;

    @ApiModelProperty("删除状态：0-正常；2-删除")
    @TableLogic
    private Integer delFlag;
}
