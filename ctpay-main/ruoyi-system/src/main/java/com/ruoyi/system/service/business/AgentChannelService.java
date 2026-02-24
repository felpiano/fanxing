package com.ruoyi.system.service.business;

import com.ruoyi.system.domain.business.AgentChannelEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 产品 服务类
 * </p>
 *
 * @author admin
 * @since 2024-10-21
 */
public interface AgentChannelService extends IService<AgentChannelEntity> {
    List<AgentChannelEntity> listByAgentId(Long agentId, String channelCode, String channelName);
}
