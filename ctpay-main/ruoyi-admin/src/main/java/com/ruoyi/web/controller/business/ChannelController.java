package com.ruoyi.web.controller.business;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.business.ChannelEntity;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.ruoyi.system.domain.business.ShopBaseChannelEntity;
import com.ruoyi.system.domain.business.ShopEntity;
import com.ruoyi.system.domain.dto.ChannelProductDTO;
import com.ruoyi.system.domain.dto.ChannelQueryDTO;
import com.ruoyi.system.mapper.business.MerchantChannelMapper;
import com.ruoyi.system.mapper.business.MerchantMapper;
import com.ruoyi.system.mapper.business.ShopMapper;
import com.ruoyi.system.service.business.ChannelService;
import com.ruoyi.system.service.business.MerchantService;
import com.ruoyi.system.service.business.ShopBaseChannelService;
import com.ruoyi.system.service.business.impl.AsyncRedisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 通道 前端控制器
 * </p>
 *
 * @author admin
 * @since 2024-09-08
 */
@RestController
@RequestMapping("/channelEntity")
@Api("通道管理")
public class ChannelController extends BaseController {
    @Resource
    private ChannelService channelService;
    @Resource
    private AsyncRedisService asyncRedisService;


    @ApiOperation("获取通道列表")
    @PreAuthorize("@ss.hasPermi('system:channel:list')")
    @PostMapping("/list")
    public R<Page<ChannelEntity>> userList(@RequestBody ChannelQueryDTO queryDTO) {
        Page<ChannelEntity> rowPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        //queryWrapper组装查询where条件
        LambdaQueryWrapper<ChannelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotEmpty(queryDTO.getChannelCode()), ChannelEntity::getChannelCode, queryDTO.getChannelCode())
                .like(StrUtil.isNotEmpty(queryDTO.getChannelName()), ChannelEntity::getChannelName, queryDTO.getChannelName())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getStatus()), ChannelEntity::getStatus, queryDTO.getStatus());
        return R.ok(channelService.page(rowPage, queryWrapper));
    }

    @Log(title = "新增通道", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('system:channel:add')")
    @ApiOperation("新增通道")
    @PostMapping("add")
    public AjaxResult add(@Validated @RequestBody ChannelEntity channelEntity) {
        if (channelEntity == null || StrUtil.isEmpty(channelEntity.getChannelCode()) || StrUtil.isEmpty(channelEntity.getChannelName())){
            return AjaxResult.error("通道编号或通道名称不允许为空");
        }
        //新增通道
        return channelService.addChannel(channelEntity);
    }

    @Log(title = "新增通道包含产品", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('system:channel:add')")
    @ApiOperation("新增通道包含产品")
    @PostMapping("addChannelProduct")
    public AjaxResult addChannelProduct(@Validated @RequestBody ChannelProductDTO dto) {
        if (dto == null || StrUtil.isEmpty(dto.getChannelCode()) || StrUtil.isEmpty(dto.getChannelName())){
            return AjaxResult.error("通道编号或通道名称不允许为空");
        }
        //新增通道
        return channelService.addChannelProduct(dto);
    }

    @Log(title = "修改通道", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('system:channel:update')")
    @ApiOperation("修改通道")
    @PostMapping("update")
    public AjaxResult update(@Validated @RequestBody ChannelEntity channelEntity) {
        if (channelEntity == null || StrUtil.isEmpty(channelEntity.getChannelCode()) || StrUtil.isEmpty(channelEntity.getChannelName())){
            return AjaxResult.error("通道编号或通道名称不允许为空");
        }
        List<ChannelEntity> list = channelService.list(Wrappers.lambdaQuery(ChannelEntity.class)
                .ne(ChannelEntity::getId, channelEntity.getId())
                .and(a -> a.eq(ChannelEntity::getChannelCode, channelEntity.getChannelCode()).or()
                        .eq(ChannelEntity::getChannelName, channelEntity.getChannelName()))
        );
        if (list != null && !list.isEmpty()) {
            return AjaxResult.error("通道编号或通道名称已存在");
        }
        boolean flag = channelService.updateById(channelEntity);
        asyncRedisService.asyncReportQrcode();
        return  flag ? AjaxResult.success():AjaxResult.error();
    }

    @Log(title = "删除通道", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('system:channel:delete')")
    @ApiOperation("删除通道")
    @GetMapping("deleteChannel")
    public AjaxResult deleteChannel(@RequestParam("id")Long id) {
        return channelService.deleteChannel(id);
    }

    @Log(title = "修改通道状态", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('system:channel:updateStatus')")
    @ApiOperation("修改通道状态")
    @GetMapping("upateStatus")
    public AjaxResult upateStatus(@RequestParam("id")Long id, @RequestParam("status")Integer status) {
        return channelService.updateChannelStatus(id, status);
    }

    @ApiOperation("获取不分页通道列表")
    @PreAuthorize("@ss.hasPermi('system:channel:listNoPage')")
    @PostMapping("/listNoPage")
    public R<List<ChannelEntity>> listNoPage(@RequestBody ChannelQueryDTO queryDTO) {
        //queryWrapper组装查询where条件
        LambdaQueryWrapper<ChannelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotEmpty(queryDTO.getChannelCode()), ChannelEntity::getChannelCode, queryDTO.getChannelCode())
                .like(StrUtil.isNotEmpty(queryDTO.getChannelName()), ChannelEntity::getChannelName, queryDTO.getChannelName())
                .eq(ChannelEntity::getStatus, 0)
                .select(ChannelEntity::getId, ChannelEntity::getChannelCode, ChannelEntity::getChannelName);
        return R.ok(channelService.list(queryWrapper));
    }
}
