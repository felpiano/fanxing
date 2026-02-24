package com.ruoyi.system.service.business;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.business.AgentEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.dto.AmountChangeDTO;

import java.math.BigDecimal;

/**
 * <p>
 * 代理基础信息表 服务类
 * </p>
 *
 * @author admin
 * @since 2024-09-08
 */
public interface AgentService extends IService<AgentEntity> {

    AjaxResult saveAgent(SysUser user);

    AjaxResult updateAgent(AgentEntity agentEntity);

    AjaxResult deleteAgent(Long userId);

    /**
     * 修改代理余额
     * @param amountChangeDTO
     * @return
     */
    AjaxResult updateAgentBalance(AmountChangeDTO amountChangeDTO);
}
