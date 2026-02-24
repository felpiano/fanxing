package com.ruoyi.system.service.business.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.ruoyi.system.domain.business.ShopMerchantRelationEntity;
import com.ruoyi.system.domain.dto.ShopMerchantRelationQueryDTO;
import com.ruoyi.system.mapper.business.MerchantMapper;
import com.ruoyi.system.mapper.business.ShopMerchantRelationMapper;
import com.ruoyi.system.service.business.MerchantService;
import com.ruoyi.system.service.business.ShopMerchantRelationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 商户与码商关系表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
@Service
public class ShopMerchantRelationServiceImp extends ServiceImpl<ShopMerchantRelationMapper, ShopMerchantRelationEntity> implements ShopMerchantRelationService {

    @Resource
    private MerchantMapper merchantMapper;
    @Resource
    private AsyncRedisService asyncRedisService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult bindShopAndMerchant(Long shopId ,String merchantIds) {
        //先清空当前码商
        baseMapper.delete(Wrappers.lambdaQuery(ShopMerchantRelationEntity.class).eq(ShopMerchantRelationEntity::getShopId, shopId));
        //绑定码商
        if (StrUtil.isNotEmpty(merchantIds)) {
            String[] ids = merchantIds.split(",");
            if (ids.length > 0) {
                List<String> merchantIdList = Arrays.asList(ids);
                List<ShopMerchantRelationEntity> relationEntities = new ArrayList<>();
                //所有1级码商
                List<MerchantEntity> merchantList = merchantMapper.selectList(Wrappers.lambdaQuery(MerchantEntity.class)
                        .eq(MerchantEntity::getAgentId, SecurityUtils.getUserId())
                        .eq(MerchantEntity::getMerchantLevel, 1)
                        .in(MerchantEntity::getUserId, merchantIdList));
                if (merchantList != null && !merchantList.isEmpty()) {
                    for (MerchantEntity id : merchantList) {
                        //获取所有下级包含自己
                        List<MerchantEntity> allChildList = merchantMapper.selectList(Wrappers.lambdaQuery(MerchantEntity.class)
                                .eq(MerchantEntity::getAgentId, SecurityUtils.getUserId())
                                .likeRight(MerchantEntity::getParentPath, id.getParentPath()));
                        allChildList.forEach(all -> {
                            ShopMerchantRelationEntity shopMerchantRelationEntity = ShopMerchantRelationEntity.builder()
                                    .shopId(shopId)
                                    .merchantId(all.getUserId())
                                    .build();
                            relationEntities.add(shopMerchantRelationEntity);
                        });
                    }
                }
                if (!relationEntities.isEmpty()) {
                    saveBatch(relationEntities);
                }
            }
        }
        asyncRedisService.asyncReportQrcode();
        return AjaxResult.success();
    }

    @Override
    public List<MerchantEntity> getRelationByShopId(Long shopId) {
        return baseMapper.getRelationByShopId(shopId);
    }

    @Override
    public Page<ShopMerchantRelationEntity> relationList(Page<ShopMerchantRelationEntity> page, ShopMerchantRelationQueryDTO queryDTO) {
        return baseMapper.relationList(page, queryDTO);
    }
}
