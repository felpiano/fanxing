package com.ruoyi.system.service.business;

import com.ruoyi.system.domain.business.BaseChannelEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 基础产品 服务类
 * </p>
 *
 * @author admin
 * @since 2024-11-02
 */
public interface BaseChannelService extends IService<BaseChannelEntity> {
    List<BaseChannelEntity> listByUser(Long agentId, String channelCode, String channelName);
}
