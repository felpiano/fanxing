package com.ruoyi.system.service.business.impl;

import com.ruoyi.system.domain.business.AgentChannelEntity;
import com.ruoyi.system.mapper.business.AgentChannelMapper;
import com.ruoyi.system.service.business.AgentChannelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 产品 服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-10-21
 */
@Service
public class AgentChannelServiceImp extends ServiceImpl<AgentChannelMapper, AgentChannelEntity> implements AgentChannelService {

    @Override
    public List<AgentChannelEntity> listByAgentId(Long agentId, String channelCode, String channelName) {
        return baseMapper.listByAgentId(agentId, channelCode, channelName);
    }
}
