package com.ruoyi.system.service.business;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.business.ShopEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.dto.AmountChangeDTO;

/**
 * <p>
 * 商户 服务类
 * </p>
 *
 * @author admin
 * @since 2024-10-18
 */

public interface ShopService extends IService<ShopEntity> {
    AjaxResult saveAgentShop(ShopEntity saveDTO);

    AjaxResult updateGoogleFlag(Long userId, Integer googleSecretFlag);

    AjaxResult deleteAgentShop(Long userId);

    AjaxResult updateShop(ShopEntity shop);

    ShopEntity getShopById(Long id);

    /**
     * 修改商户余额
     * @param shopAmountDTO
     */
    void updateShopAmount(AmountChangeDTO shopAmountDTO);
}
