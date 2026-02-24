package com.ruoyi.system.service.business.impl;

import com.ruoyi.system.domain.business.BaseChannelEntity;
import com.ruoyi.system.mapper.business.BaseChannelMapper;
import com.ruoyi.system.service.business.BaseChannelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 基础产品 服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-11-02
 */
@Service
public class BaseChannelServiceImp extends ServiceImpl<BaseChannelMapper, BaseChannelEntity> implements BaseChannelService {

    @Override
    public List<BaseChannelEntity> listByUser(Long agentId, String channelCode, String channelName) {
        return baseMapper.listByUser(channelCode, channelName);
    }
}
