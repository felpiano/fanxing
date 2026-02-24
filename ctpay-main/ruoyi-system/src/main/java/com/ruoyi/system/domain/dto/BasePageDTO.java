package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author admin
 * &#064;Date  2024/9/8 2:22
 */
@ApiModel("基础分页查询")
@Data
public class BasePageDTO {

    @ApiModelProperty("页数")
    private Integer pageNum = 1;

    @ApiModelProperty("每页条数")
    private Integer pageSize = 10;
}
