package com.ruoyi.system.mapper.business;

import com.ruoyi.system.domain.business.MerchantChannelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 码商通道费率 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
public interface MerchantChannelMapper extends BaseMapper<MerchantChannelEntity> {
    List<MerchantChannelEntity> listByMerchantId(@Param("merchantId")Long merchantId, @Param("channelId")Long channelId);

}
