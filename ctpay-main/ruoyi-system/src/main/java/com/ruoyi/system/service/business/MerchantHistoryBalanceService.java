package com.ruoyi.system.service.business;

import com.ruoyi.system.domain.business.MerchantHistoryBalanceEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 码商每日余额历史 服务类
 * </p>
 *
 * @author admin
 * @since 2024-11-06
 */
public interface MerchantHistoryBalanceService extends IService<MerchantHistoryBalanceEntity> {
    void init(String date);

    void everyDayTask();
}
