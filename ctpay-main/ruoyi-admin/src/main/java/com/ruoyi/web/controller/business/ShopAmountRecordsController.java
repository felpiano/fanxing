package com.ruoyi.web.controller.business;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.business.AgentAmountRecordsEntity;
import com.ruoyi.system.domain.business.MerchantAmountRecordsEntity;
import com.ruoyi.system.domain.business.ShopAmountRecordsEntity;
import com.ruoyi.system.domain.dto.AmountChangeQueryDTO;
import com.ruoyi.system.service.business.ShopAmountRecordsService;
import io.swagger.annotations.Api;
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
@Api("商户资金记录")
@RequestMapping("/shopAmountRecordsEntity")
public class ShopAmountRecordsController extends BaseController {

    @Resource
    private ShopAmountRecordsService shopAmountRecordsService;

    @ApiOperation("获取商户帐变列表")
    @PreAuthorize("@ss.hasPermi('system:shop:list')")
    @PostMapping("/list")
    public R<Page<ShopAmountRecordsEntity>> userList(@RequestBody AmountChangeQueryDTO queryDTO) {
        Page<ShopAmountRecordsEntity> rowPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        //queryWrapper组装查询where条件
        LambdaQueryWrapper<ShopAmountRecordsEntity> queryWrapper = Wrappers.lambdaQuery(ShopAmountRecordsEntity.class)
                .eq(ObjectUtil.isNotEmpty(queryDTO.getUserId()), ShopAmountRecordsEntity::getUserId, queryDTO.getUserId())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getOrderNo()), ShopAmountRecordsEntity::getOrderNo, queryDTO.getOrderNo())
                .like(StrUtil.isNotEmpty(queryDTO.getUserName()), ShopAmountRecordsEntity::getUserName, queryDTO.getUserName())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getChangeType()), ShopAmountRecordsEntity::getChangeType, queryDTO.getChangeType())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getAmountType()), ShopAmountRecordsEntity::getAmountType, queryDTO.getAmountType())
                .ge(ObjectUtil.isNotEmpty(queryDTO.getMinAmount()), ShopAmountRecordsEntity::getChangeAmount, queryDTO.getMinAmount())
                .le(ObjectUtil.isNotEmpty(queryDTO.getMaxAmount()), ShopAmountRecordsEntity::getChangeAmount, queryDTO.getMaxAmount())
                .ge(ObjectUtil.isNotEmpty(queryDTO.getStartTime()), ShopAmountRecordsEntity::getCreateTime, queryDTO.getStartTime())
                .le(ObjectUtil.isNotEmpty(queryDTO.getEndTime()), ShopAmountRecordsEntity::getCreateTime, queryDTO.getEndTime())
                .orderByDesc(ShopAmountRecordsEntity::getId);
        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        if (sysUser != null) {
            if (sysUser.getIdentity() == 3) {
                queryWrapper.eq(ShopAmountRecordsEntity::getAgentId, sysUser.getUserId());
            } else if (sysUser.getIdentity() == 1) {
                queryWrapper.eq(ShopAmountRecordsEntity::getUserId, sysUser.getUserId());
            }
            return R.ok(shopAmountRecordsService.page(rowPage, queryWrapper));
        }
        return R.ok(new Page<>());
    }
}
