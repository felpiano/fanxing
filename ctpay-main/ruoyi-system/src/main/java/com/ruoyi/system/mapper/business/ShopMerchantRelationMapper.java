package com.ruoyi.system.mapper.business;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.ruoyi.system.domain.business.ShopMerchantRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.dto.ShopMerchantRelationQueryDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 商户与码商关系表 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
public interface ShopMerchantRelationMapper extends BaseMapper<ShopMerchantRelationEntity> {
    //仅查询一级码商
    List<MerchantEntity> getRelationByShopId(@Param("shopId")Long shopId);

    Page<ShopMerchantRelationEntity> relationList(Page<ShopMerchantRelationEntity> page, @Param("dto") ShopMerchantRelationQueryDTO queryDTO);
}
