package com.ruoyi.web.controller.business;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.business.CtOnebuyBlack;
import com.ruoyi.system.domain.dto.OnebuyBlackQueryDTO;
import com.ruoyi.system.service.business.ICtOnebuyBlackService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 一码通黑名单Controller
 *
 * @author ruoyi
 * @date 2025-12-02
 */
@RestController
@RequestMapping("/system/onebuyBlack")
public class CtOnebuyBlackController extends BaseController {
    @Autowired
    private ICtOnebuyBlackService ctOnebuyBlackService;
    

    @ApiOperation("获取一码通黑名单列表")
    @PreAuthorize("@ss.hasPermi('system:onebuyBlack:list')")
    @PostMapping("/list")
    public R<Page<CtOnebuyBlack>> userList(@RequestBody OnebuyBlackQueryDTO queryDTO) {
        Page<CtOnebuyBlack> rowPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        //queryWrapper组装查询where条件
        LambdaQueryWrapper<CtOnebuyBlack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotEmpty(queryDTO.getPayer()), CtOnebuyBlack::getPayer, queryDTO.getPayer());
        return R.ok(ctOnebuyBlackService.page(rowPage, queryWrapper));
    }


    @ApiOperation("根据id获取一码通黑名单")
    @PreAuthorize("@ss.hasPermi('system:onebuyBlack:get')")
    @GetMapping("/getById")
    public R<CtOnebuyBlack> getById(@RequestParam("id") Long id) {
        return R.ok(ctOnebuyBlackService.getById(id));
    }


    @Log(title = "新增一码通黑名单", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('system:onebuyBlack:add')")
    @ApiOperation("新增一码通黑名单")
    @PostMapping("add")
    public AjaxResult add(@Validated @RequestBody CtOnebuyBlack ctOnebuyBlack) {
        if (ctOnebuyBlack == null || StrUtil.isEmpty(ctOnebuyBlack.getPayer())){
            return AjaxResult.error("黑名单用户标识不允许为空");
        }
        List<CtOnebuyBlack> list = ctOnebuyBlackService.list(Wrappers.lambdaQuery(CtOnebuyBlack.class)
                .and(a -> a.eq(CtOnebuyBlack::getPayer, ctOnebuyBlack.getPayer()))
        );
        if (list != null && !list.isEmpty()) {
            return AjaxResult.error("黑名单用户标识已存在");
        }
        ctOnebuyBlackService.save(ctOnebuyBlack);
        return AjaxResult.success();
    }

    @Log(title = "修改一码通黑名单", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('system:onebuyBlack:update')")
    @ApiOperation("修改一码通黑名单")
    @PostMapping("update")
    public AjaxResult update(@Validated @RequestBody CtOnebuyBlack ctOnebuyBlack) {
        if (ctOnebuyBlack == null || StrUtil.isEmpty(ctOnebuyBlack.getPayer()) ){
            return AjaxResult.error("黑名单用户标识不允许为空");
        }
        List<CtOnebuyBlack> list = ctOnebuyBlackService.list(Wrappers.lambdaQuery(CtOnebuyBlack.class)
                .ne(CtOnebuyBlack::getId, ctOnebuyBlack.getId())
                .and(a -> a.eq(CtOnebuyBlack::getPayer, ctOnebuyBlack.getPayer()))
        );
        if (list != null && !list.isEmpty()) {
            return AjaxResult.error("黑名单用户标识已存在");
        }
        boolean flag = ctOnebuyBlackService.updateById(ctOnebuyBlack);
        return  flag ? AjaxResult.success():AjaxResult.error();
    }

    @Log(title = "删除一码通黑名单", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('system:onebuyBlack:delete')")
    @ApiOperation("删除一码通黑名单")
    @GetMapping("delete")
    public AjaxResult delete(@RequestParam("ids")String ids) {
        if (StrUtil.isBlank(ids)) {
            return AjaxResult.error("未指定需要删除的数据");
        }
        ctOnebuyBlackService.removeByIds(StrUtil.split(ids, ","));
        return AjaxResult.success();
    }

    
}
