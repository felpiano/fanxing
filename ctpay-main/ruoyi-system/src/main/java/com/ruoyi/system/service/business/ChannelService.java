package com.ruoyi.system.service.business;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.business.BaseChannelEntity;
import com.ruoyi.system.domain.business.ChannelEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.dto.ChannelProductDTO;

/**
 * <p>
 * 通道 服务类
 * </p>
 *
 * @author admin
 * @since 2024-09-08
 */
public interface ChannelService extends IService<ChannelEntity> {
    /**
     *@author admin
     *@Date 2024/9/10 21:04
     *@Description 新增通道
     *
     */
    AjaxResult addChannel(ChannelEntity channelEntity);

    /**
     *@author admin
     *@Date 2024/9/10 21:48
     *@Description 修改通道
     *
     */
    AjaxResult deleteChannel(Long id);


    /**
     * 修改通道状态
     * @param id
     * @param status
     * @return
     */
    AjaxResult updateChannelStatus(Long id, Integer status);

    /**
     * 新增通道及产品
     * @param dto
     * @return
     */
    AjaxResult addChannelProduct(ChannelProductDTO dto);

    AjaxResult updateBaseChannel(BaseChannelEntity baseChannelEntity);
}
