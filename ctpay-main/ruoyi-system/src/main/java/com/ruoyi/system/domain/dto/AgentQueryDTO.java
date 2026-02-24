package com.ruoyi.system.domain.dto;
import io.swagger.annotations.ApiModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author admin
 * &#064;Date  2024/9/8 21:42
 */
@Getter
@Setter
@Schema(description = "代理管理查询请求")
public class AgentQueryDTO extends BasePageDTO{
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户登录名")
    private String userName;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "帐号状态（0正常 1停用）")
    private Integer status;

    @Schema(description = "删除标志（0代表存在 2代表删除）")
    private Integer delFlag;
}
