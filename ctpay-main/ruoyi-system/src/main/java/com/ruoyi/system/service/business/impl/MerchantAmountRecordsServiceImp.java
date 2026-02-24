package com.ruoyi.system.service.business.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.system.domain.business.MerchantAmountRecordsEntity;
import com.ruoyi.system.domain.dto.AmountChangeQueryDTO;
import com.ruoyi.system.mapper.business.MerchantAmountRecordsMapper;
import com.ruoyi.system.service.business.MerchantAmountRecordsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
@Service
public class MerchantAmountRecordsServiceImp extends ServiceImpl<MerchantAmountRecordsMapper, MerchantAmountRecordsEntity> implements MerchantAmountRecordsService {

    @Override
    public Page<MerchantAmountRecordsEntity> amountRecords(Page<MerchantAmountRecordsEntity> page, AmountChangeQueryDTO dto) {
        return baseMapper.amountRecords(page, dto);
    }

    @Override
    public List<MerchantAmountRecordsEntity> amountRecords(AmountChangeQueryDTO dto) {
        return baseMapper.amountRecords(dto);
    }
}
