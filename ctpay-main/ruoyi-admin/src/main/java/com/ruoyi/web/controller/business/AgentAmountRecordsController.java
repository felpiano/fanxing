package com.ruoyi.web.controller.business;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.business.AgentAmountRecordsEntity;
import com.ruoyi.system.domain.business.AgentEntity;
import com.ruoyi.system.domain.dto.AgentQueryDTO;
import com.ruoyi.system.domain.dto.AmountChangeQueryDTO;
import com.ruoyi.system.service.business.AgentAmountRecordsService;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import com.ruoyi.common.core.controller.BaseController;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
@RestController
@RequestMapping("/agentAmountRecordsEntity")
@ApiModel("代理商资金记录")
public class AgentAmountRecordsController extends BaseController {

    @Resource
    private AgentAmountRecordsService agentAmountRecordsService;

    @ApiOperation("获取代理帐变列表")
    @PreAuthorize("@ss.hasPermi('system:agent:list')")
    @PostMapping("/list")
    public R<Page<AgentAmountRecordsEntity>> userList(@RequestBody AmountChangeQueryDTO queryDTO) {
        Page<AgentAmountRecordsEntity> rowPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        //queryWrapper组装查询where条件
        LambdaQueryWrapper<AgentAmountRecordsEntity> queryWrapper = Wrappers.lambdaQuery(AgentAmountRecordsEntity.class)
                .eq(ObjectUtil.isNotEmpty(queryDTO.getUserId()), AgentAmountRecordsEntity::getUserId, queryDTO.getUserId())
                .like(StrUtil.isNotEmpty(queryDTO.getUserName()), AgentAmountRecordsEntity::getUserName, queryDTO.getUserName())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getChangeType()), AgentAmountRecordsEntity::getChangeType, queryDTO.getChangeType())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getAmountType()), AgentAmountRecordsEntity::getAmountType, queryDTO.getAmountType())
                .ge(ObjectUtil.isNotEmpty(queryDTO.getMinAmount()), AgentAmountRecordsEntity::getChangeAmount, queryDTO.getMinAmount())
                .le(ObjectUtil.isNotEmpty(queryDTO.getMaxAmount()), AgentAmountRecordsEntity::getChangeAmount, queryDTO.getMaxAmount())
                .ge(ObjectUtil.isNotEmpty(queryDTO.getStartTime()), AgentAmountRecordsEntity::getCreateTime, queryDTO.getStartTime())
                .le(ObjectUtil.isNotEmpty(queryDTO.getEndTime()), AgentAmountRecordsEntity::getCreateTime, queryDTO.getEndTime())
                .orderByDesc(AgentAmountRecordsEntity::getId);
                ;
        queryWrapper.eq(!SecurityUtils.isAdmin(SecurityUtils.getLoginUser().getUser().getRoles()),AgentAmountRecordsEntity::getUserId, SecurityUtils.getLoginUser().getUserId());
        return R.ok(agentAmountRecordsService.page(rowPage, queryWrapper));
    }
}
