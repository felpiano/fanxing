package com.ruoyi.system.service.business;

import com.ruoyi.system.domain.business.ShopBaseChannelEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 商户通道费率 服务类
 * </p>
 *
 * @author admin
 * @since 2024-10-18
 */
public interface ShopBaseChannelService extends IService<ShopBaseChannelEntity> {

    List<ShopBaseChannelEntity> listByShopId(Long shopId);
}
