package com.ruoyi.web.controller.business;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.business.ShopBaseChannelEntity;
import com.ruoyi.system.service.business.ShopBaseChannelService;
import com.ruoyi.system.service.business.impl.AsyncRedisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 商户基础通道费率 前端控制器
 * </p>
 *
 * @author admin
 * @since 2024-10-14
 */
@RestController
@RequestMapping("/shopBaseChannelEntity")
@Api("商户基础通道")
public class ShopBaseChannelController extends BaseController {
    @Resource
    private ShopBaseChannelService baseChannelService;
    @Resource
    private AsyncRedisService asyncRedisService;


    @ApiOperation("商户基础通道列表")
    @PreAuthorize("@ss.hasPermi('system:shopBase:list')")
    @ApiParam(name = "shopId", value = "商户ID", required = true)
    @GetMapping("list")
    public R<List<ShopBaseChannelEntity>> list(@RequestParam("shopId")Long shopId) {
        List<ShopBaseChannelEntity> list = baseChannelService.listByShopId(shopId);
        return R.ok(list);
    }

    @Log(title = "修改商户基础通道列表", businessType = BusinessType.UPDATE)
    @ApiOperation("修改商户基础通道列表")
    @PreAuthorize("@ss.hasPermi('system:shopBase:update')")
    @PostMapping("updateBatch")
    public AjaxResult updateBatch(@RequestBody List<ShopBaseChannelEntity> list) {
        baseChannelService.updateBatchById(list);
        asyncRedisService.asyncReportQrcode();
        return AjaxResult.success();
    }
}
