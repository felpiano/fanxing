package com.ruoyi.web.controller.business;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.business.MerchantChannelEntity;
import com.ruoyi.system.service.business.MerchantChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 码商通道费率 前端控制器
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
@RestController
@RequestMapping("/merchantChannelEntity")
@Api("码商通道")
public class MerchantChannelController extends BaseController {

    @Resource
    private MerchantChannelService merchantChannelService;

    @ApiOperation("码商通道列表")
    @PreAuthorize("@ss.hasPermi('system:merchantChannel:list')")
    @ApiParam(name = "merchantId", value = "ID", required = true)
    @GetMapping("list")
    public R<List<MerchantChannelEntity>> list(@RequestParam("merchantId")Long merchantId) {
        List<MerchantChannelEntity> list = merchantChannelService.listByMerchantId(merchantId, null);
        return R.ok(list);
    }

    @Log(title = "修改码商通道列表", businessType = BusinessType.UPDATE)
    @ApiOperation("修改码商通道列表")
    @PreAuthorize("@ss.hasPermi('system:merchantChannel:update')")
    @PostMapping("updateBatch")
    public AjaxResult updateBatch(@RequestBody List<MerchantChannelEntity> merchantChannelEntityList) {
        merchantChannelService.updateMerchantChannelBatch(merchantChannelEntityList);
        return AjaxResult.success();
    }

    @Log(title = "单独修改码商通道费率", businessType = BusinessType.UPDATE)
    @ApiOperation("单独修改码商通道费率")
    @PreAuthorize("@ss.hasPermi('system:merchantChannel:update')")
    @PostMapping("updateChannelRate")
    public AjaxResult updateChannelRate(@RequestBody MerchantChannelEntity merchantChannelEntity) {
        merchantChannelService.updateMerchantChannel(merchantChannelEntity);
        return AjaxResult.success();
    }

    @ApiOperation("码商查看自身通道列表")
    @PreAuthorize("@ss.hasPermi('system:merchantChannel:selfChannel')")
    @ApiParam(name = "channelId", value = "通道ID", required = false)
    @GetMapping("selfChannel")
    public R<List<MerchantChannelEntity>> selfChannel(@RequestParam(value = "channelId", required = false)Long channelId) {
        List<MerchantChannelEntity> list = merchantChannelService.listByMerchantId(SecurityUtils.getUserId(), channelId);
        return R.ok(list);
    }

    @ApiOperation("代理关闭1级码商级其下级的通道")
    @GetMapping("agentContrl")
    public AjaxResult agentContrl(@RequestParam(value = "merchantId")Long merchantId,@RequestParam(value = "channelId")Long channelId, @RequestParam(value = "agentContrl")Integer agentContrl) {
        try {
            merchantChannelService.agentContrl(merchantId, channelId, agentContrl);
        }catch (Exception e){
            logger.error("关闭失败，{}",e.getMessage());
            return AjaxResult.error("更新失败");
        }
        return AjaxResult.success();
    }
}
