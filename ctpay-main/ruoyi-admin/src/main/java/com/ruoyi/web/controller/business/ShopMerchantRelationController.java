package com.ruoyi.web.controller.business;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.business.ShopMerchantRelationEntity;
import com.ruoyi.system.domain.dto.ShopMerchantRelationQueryDTO;
import com.ruoyi.system.service.business.ShopMerchantRelationService;
import com.ruoyi.system.service.business.impl.AsyncRedisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/shopMerchantRelation")
@Api("商户码商关联管理")
public class ShopMerchantRelationController extends BaseController {
    @Resource
    private ShopMerchantRelationService shopMerchantRelationService;
    @Resource
    private AsyncRedisService asyncRedisService;

    @ApiOperation("获取商户码商关联列表")
    @PostMapping("/list")
    public R<Page<ShopMerchantRelationEntity>> userList(@RequestBody ShopMerchantRelationQueryDTO queryDTO) {
        Page<ShopMerchantRelationEntity> rowPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        return R.ok(shopMerchantRelationService.relationList(rowPage, queryDTO));
    }

    @Log(title = "修改商户码商关联关系的状态", businessType = BusinessType.UPDATE)
    @ApiOperation("修改商户码商关联关系的状态")
    @GetMapping("updateStatus")
    public AjaxResult updateStatus(@RequestParam("id")Long id, @RequestParam("status")Integer status) {
        ShopMerchantRelationEntity shopMerchantRelationEntity = new ShopMerchantRelationEntity();
        shopMerchantRelationEntity.setId(id);
        shopMerchantRelationEntity.setStatus(status);
        boolean flag = shopMerchantRelationService.updateById(shopMerchantRelationEntity);
        asyncRedisService.asyncReportQrcode();
        return flag?AjaxResult.success():AjaxResult.error();
    }
}


