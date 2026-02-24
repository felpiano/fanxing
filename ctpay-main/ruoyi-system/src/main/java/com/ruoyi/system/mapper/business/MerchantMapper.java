package com.ruoyi.system.mapper.business;

import com.ruoyi.system.domain.business.MerchantChannelEntity;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.vo.MerchantDepositVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 码商 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
public interface MerchantMapper extends BaseMapper<MerchantEntity> {
    List<MerchantDepositVO> listBaseDeposit();

    List<MerchantChannelEntity> merchantChannelList(@Param("agentId") Long agentId, @Param("channelId") Long channelId);
}
