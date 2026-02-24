package com.ruoyi.system.service.business.impl;

import com.ruoyi.system.domain.business.ShopBaseChannelEntity;
import com.ruoyi.system.mapper.business.ShopBaseChannelMapper;
import com.ruoyi.system.service.business.ShopBaseChannelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 商户通道费率 服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-10-18
 */
@Service
public class ShopBaseChannelServiceImp extends ServiceImpl<ShopBaseChannelMapper, ShopBaseChannelEntity> implements ShopBaseChannelService {

    @Override
    public List<ShopBaseChannelEntity> listByShopId(Long shopId) {
        return baseMapper.listByShopId(shopId);
    }
}
