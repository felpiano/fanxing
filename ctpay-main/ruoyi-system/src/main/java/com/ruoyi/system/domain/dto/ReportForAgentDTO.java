package com.ruoyi.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("代理对账")
public class ReportForAgentDTO {
    private Long agentId;
    @ApiModelProperty("码商ID")
    private Long merchantId;
    @ApiModelProperty("码商")
    private String merchantName;
    @ApiModelProperty("商户ID")
    private Long shopId;
    @ApiModelProperty("商户")
    private String shopName;

    @ApiModelProperty("类型：1-商户；2-日期；3-通道。（统计报表需要）")
    private int type;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    @ApiModelProperty("1级码商ID")
    private Long merchantIdOne;

    @ApiModelProperty("1级码商名称")
    private String merchantNameOne;

    private String childPathStart;

    private Integer merchantLevel;

    private Long neMerchantId;

    @ApiModelProperty("通道ID")
    private Long channelId;

    @ApiModelProperty("是否有跑量:1-是")
    private Integer orderFlag;
}
