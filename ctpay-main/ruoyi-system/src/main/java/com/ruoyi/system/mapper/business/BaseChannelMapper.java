package com.ruoyi.system.mapper.business;

import com.ruoyi.system.domain.business.BaseChannelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 基础产品 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2024-11-02
 */
public interface BaseChannelMapper extends BaseMapper<BaseChannelEntity> {
    List<BaseChannelEntity> listByUser(@Param("channelCode")String channelCode, @Param("channelName")String channelName);
}
