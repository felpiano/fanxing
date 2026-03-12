package com.ruoyi.system.service.business;

import com.ruoyi.system.domain.business.MerchantChannelEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.business.MerchantEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 码商通道费率 服务类
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
public interface MerchantChannelService extends IService<MerchantChannelEntity> {
    List<MerchantChannelEntity> listByMerchantId(Long merchantId, Long channelId);

    void updateMerchantChannelBatch(List<MerchantChannelEntity> list);

    /**
     * 获取所有下级码商
     * @param merchantEntity
     * @return
     */
    List<MerchantEntity> listAllChild(MerchantEntity merchantEntity);

    /**
     * 修改码商费率
     * @param merchantChannel
     */
    void updateMerchantChannel(MerchantChannelEntity merchantChannel);

    /**
     * 批量修改码商费率（同一通道，多个同级别码商）
     * 校验：费率不能超过父级（取父级对应层级费率最小值），
     *       费率不能低于下级码商（取子级对应层级费率最大值）
     * @param list 码商通道列表（同一通道，不同码商）
     * @param currLevel 码商层级，如2代表入参都是二级码商
     * @param channelRate 要设置的费率
     */
    void updateMerchantChannelList(List<MerchantChannelEntity> list, Integer currLevel, BigDecimal channelRate);

    /**
     * 关闭一级码商及其下级的通道
     * @param merchantId
     * @param channelId
     * @param agentContrl
     */
    void agentContrl(Long merchantId,Long channelId,Integer agentContrl);
}
