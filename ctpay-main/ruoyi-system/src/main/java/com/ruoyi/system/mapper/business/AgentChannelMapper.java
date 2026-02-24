package com.ruoyi.system.mapper.business;

import com.ruoyi.system.domain.business.AgentChannelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 产品 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2024-10-21
 */
public interface AgentChannelMapper extends BaseMapper<AgentChannelEntity> {
    List<AgentChannelEntity> listByAgentId(@Param("agentId")Long agentId, @Param("channelCode")String channelCode, @Param("channelName")String channelName);
}
