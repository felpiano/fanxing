package com.ruoyi.web.controller.business;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.business.InOrderEntity;
import com.ruoyi.system.domain.business.ReqOrderEntity;
import com.ruoyi.system.domain.dto.OrderQueryDTO;
import com.ruoyi.system.service.business.ReqOrderService;
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
 * 订单请求 前端控制器
 * </p>
 *
 * @author admin
 * @since 2024-11-18
 */
@RestController
@Api("进单请求")
@RequestMapping("/reqOrderEntity")
public class ReqOrderController extends BaseController {
    @Resource
    private ReqOrderService reqOrderService;

    @ApiOperation("获取请求订单")
    @PreAuthorize("@ss.hasPermi('system:reqOrder:list')")
    @PostMapping("/list")
    public R<Page<ReqOrderEntity>> list(@RequestBody OrderQueryDTO queryDTO) {
        Page<ReqOrderEntity> rowPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        //queryWrapper组装查询where条件
        LambdaQueryWrapper<ReqOrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotEmpty(queryDTO.getShopOrderNo()), ReqOrderEntity::getOutTradeNo, queryDTO.getShopOrderNo())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getChannelCode()), ReqOrderEntity::getChannel, queryDTO.getChannelCode())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getShopId()), ReqOrderEntity::getMchid, queryDTO.getShopId())
                .orderByDesc(ReqOrderEntity::getCreateTime);
        return R.ok(reqOrderService.page(rowPage, queryWrapper));
    }
}
