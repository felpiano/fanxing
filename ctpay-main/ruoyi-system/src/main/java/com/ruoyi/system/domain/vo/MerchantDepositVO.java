package com.ruoyi.system.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantDepositVO {
    private Long userId;
    private BigDecimal baseDeposit;
}
