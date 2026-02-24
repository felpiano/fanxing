package com.ruoyi.system.domain.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author admin
 * @Date 2024/9/17 3:45
 */
@Data
@ApiModel("商户回调返回")
public class ShopCallBackVO {
    private int status;

    private String order_no;

    private String amount;

    private String pay_amount;

    private String sign;
}
