package com.ruoyi.system.service.business;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.system.domain.business.MerchantAmountRecordsEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.dto.AmountChangeQueryDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
public interface MerchantAmountRecordsService extends IService<MerchantAmountRecordsEntity> {
    Page<MerchantAmountRecordsEntity> amountRecords(Page<MerchantAmountRecordsEntity> page, AmountChangeQueryDTO dto);

    List<MerchantAmountRecordsEntity> amountRecords(AmountChangeQueryDTO dto);
}
