package com.ruoyi.system.service.business;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.ruoyi.system.domain.business.ShopMerchantRelationEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.dto.ShopMerchantRelationQueryDTO;
import com.ruoyi.system.domain.vo.OrderVO;

import java.util.List;

/**
 * <p>
 * 商户与码商关系表 服务类
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
public interface ShopMerchantRelationService extends IService<ShopMerchantRelationEntity> {

    /**
     * 商户绑定码商
     * @param merchantIds
     * @return
     */
    AjaxResult bindShopAndMerchant(Long shopId ,String merchantIds);

    List<MerchantEntity> getRelationByShopId(Long shopId);

    Page<ShopMerchantRelationEntity> relationList(Page<ShopMerchantRelationEntity> page, ShopMerchantRelationQueryDTO queryDTO);
}
