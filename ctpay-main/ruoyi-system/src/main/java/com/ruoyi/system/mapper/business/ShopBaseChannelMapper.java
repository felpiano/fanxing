package com.ruoyi.system.mapper.business;

import com.ruoyi.system.domain.business.ShopBaseChannelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 商户通道费率 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2024-10-18
 */
public interface ShopBaseChannelMapper extends BaseMapper<ShopBaseChannelEntity> {
    List<ShopBaseChannelEntity> listByShopId(Long shopId);
}
