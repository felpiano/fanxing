package com.ruoyi.system.service.business;

import com.ruoyi.system.domain.business.MerchantChannelEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.business.MerchantEntity;

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
     * 关闭一级码商及其下级的通道
     * @param merchantId
     * @param channelId
     * @param agentContrl
     */
    void agentContrl(Long merchantId,Long channelId,Integer agentContrl);
}
