package com.ruoyi.web.controller.business;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.enums.SysRoleEnum;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.business.AgentChannelEntity;
import com.ruoyi.system.domain.business.ChannelEntity;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.ruoyi.system.domain.business.MerchantQrcodeEntity;
import com.ruoyi.system.domain.dto.MerchantQrcodeQueryDTO;
import com.ruoyi.system.domain.dto.MerchantQrcodeSaveDTO;
import com.ruoyi.system.domain.dto.MerchantQueryDTO;
import com.ruoyi.system.domain.vo.MerchantOrderReportVO;
import com.ruoyi.system.service.business.*;
import com.ruoyi.system.service.business.impl.AsyncRedisService;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.lang.ref.WeakReference;
import java.nio.channels.Channel;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 码商上码 前端控制器
 * </p>
 *
 * @author admin
 * @since 2024-10-20
 */
@RestController
@RequestMapping("/merchantQrcodeEntity")
@ApiModel("码商上码")
public class MerchantQrcodeController extends BaseController {

    @Resource
    private MerchantQrcodeService merchantQrcodeService;
    @Resource
    private InOrderDetailService detailService;
    @Resource
    private ChannelService channelService;
    @Resource
    private AsyncRedisService asyncRedisService;
    @Resource
    private AgentChannelService agentChannelService;
    @Resource
    private MerchantService merchantService;

    @ApiOperation("获取码商列表")
    @PreAuthorize("@ss.hasPermi('system:merchantQrcode:list')")
    @PostMapping("/list")
    public R<Page<MerchantQrcodeEntity>> userList(@RequestBody MerchantQrcodeQueryDTO queryDTO) {
        Page<MerchantQrcodeEntity> rowPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        //queryWrapper组装查询where条件
        LoginUser securityUser = SecurityUtils.getLoginUser();
        if (securityUser.getUser().getRoles() != null) {
            LambdaQueryWrapper<MerchantQrcodeEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ObjectUtil.isNotEmpty(queryDTO.getChannelId()), MerchantQrcodeEntity::getChannelId, queryDTO.getChannelId());
            queryWrapper.like(StrUtil.isNotEmpty(queryDTO.getNickName()), MerchantQrcodeEntity::getNickName, queryDTO.getNickName())
                    .eq(StrUtil.isNotEmpty(queryDTO.getAccountNumber()), MerchantQrcodeEntity::getAccountNumber, queryDTO.getAccountNumber())
                    .like(StrUtil.isNotEmpty(queryDTO.getAccountRemark()), MerchantQrcodeEntity::getAccountRemark, queryDTO.getAccountRemark())
                    .eq(ObjectUtil.isNotEmpty(queryDTO.getStatus()), MerchantQrcodeEntity::getStatus, queryDTO.getStatus());
            String roleKeys = securityUser.getUser().getRoles().stream().map(SysRole::getRoleKey).collect(Collectors.joining(","));
            if (roleKeys.contains(SysRoleEnum.MERCHANT.getRoleKey()) && ObjectUtil.isEmpty(queryDTO.getMerchantId())) {
                queryWrapper.eq(MerchantQrcodeEntity::getMerchantId, securityUser.getUserId());
            } else {
                queryWrapper.eq(MerchantQrcodeEntity::getMerchantId, queryDTO.getMerchantId());
            }
            Page<MerchantQrcodeEntity> page = merchantQrcodeService.page(rowPage, queryWrapper);
            if (ObjectUtil.isNotEmpty(page.getRecords())) {
                String startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(new Date()));
                String endTime = DateUtil.formatDateTime(DateUtil.endOfDay(new Date()));
                List<MerchantOrderReportVO> todayList = detailService.qrcodeOrders(page.getRecords().stream().map(MerchantQrcodeEntity::getMerchantId).collect(Collectors.toList()), startTime,endTime);
                Map<Long, MerchantOrderReportVO> todayMap = new HashMap<>();
                if (todayList != null && !todayList.isEmpty()) {
                    todayMap = todayList.stream().collect(Collectors.toMap(MerchantOrderReportVO::getQrcodeId, a -> a, (key1,key2)->key2));
                }

                String yesStartTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateUtil.offsetDay(new Date(), -1)));
                String yesEndTime = DateUtil.formatDateTime(DateUtil.endOfDay(DateUtil.offsetDay(new Date(), -1)));
                List<MerchantOrderReportVO> yesList = detailService.qrcodeOrders(page.getRecords().stream().map(MerchantQrcodeEntity::getMerchantId).collect(Collectors.toList()), yesStartTime,yesEndTime);
                Map<Long, MerchantOrderReportVO> yesMap = new HashMap<>();
                if (yesList != null && !yesList.isEmpty()) {
                    yesMap = yesList.stream().collect(Collectors.toMap(MerchantOrderReportVO::getQrcodeId, a -> a, (key1,key2)->key2));
                }

                List<ChannelEntity> channelList = channelService.list();
                Map<Long, ChannelEntity> channelMap = new HashMap<>();
                if (channelList != null && !channelList.isEmpty()) {
                    channelMap = channelList.stream().collect(Collectors.toMap(ChannelEntity::getId, a-> a, (key1,key2)->key2));
                }
                for (MerchantQrcodeEntity record : page.getRecords()) {
                    if (todayMap.get(record.getId()) != null) {
                        record.setSuccessAmount(todayMap.get(record.getId()).getTotalAmount());
                        record.setSuccessCount(todayMap.get(record.getId()).getTotalCount());
                    }
                    if (yesMap.get(record.getId()) != null) {
                        record.setYesSuccessAmount(yesMap.get(record.getId()).getTotalAmount());
                        record.setYesSuccessCount(yesMap.get(record.getId()).getTotalCount());
                    }
                    if (channelMap.get(record.getChannelId()) != null) {
                        record.setChannelCode(channelMap.get(record.getChannelId()).getChannelCode());
                        record.setChannelName(channelMap.get(record.getChannelId()).getChannelName());
                    }
                }
            }
            return R.ok(page);
        }
       return R.fail("暂无权限");
    }


    @GetMapping("getById")
    public R<MerchantQrcodeEntity> getById(Long id) {
        return R.ok(merchantQrcodeService.getById(id));
    }

    @Log(title = "新增或修改码", businessType = BusinessType.INSERT)
    @ApiOperation("新增或修改码")
    @PreAuthorize("@ss.hasPermi('system:merchantQrcode:add')")
    @PostMapping("add")
    public AjaxResult add(@Validated @RequestBody MerchantQrcodeSaveDTO saveDTO) {
        return merchantQrcodeService.saveMerchantQrcode(saveDTO);
    }

    @Log(title = "删除码", businessType = BusinessType.UPDATE)
    @ApiOperation("删除码")
    @PreAuthorize("@ss.hasPermi('system:merchantQrcode:delete')")
    @GetMapping("delete")
    public AjaxResult delete(@RequestParam("id") Long id) {
        boolean flag = merchantQrcodeService.removeById(id);
        asyncRedisService.asyncReportQrcode();
        return flag?AjaxResult.success():AjaxResult.error();
    }

    @Log(title = "修改码进单状态", businessType = BusinessType.UPDATE)
    @ApiOperation("修改码进单状态")
    @PreAuthorize("@ss.hasPermi('system:merchantQrcode:update')")
    @GetMapping("update")
    public AjaxResult update(@RequestParam("id")Long id, @RequestParam("status")Integer status) {
        MerchantQrcodeEntity update = MerchantQrcodeEntity.builder()
                .id(id)
                .status(status)
                .build();
        boolean flag = merchantQrcodeService.updateById(update);
        asyncRedisService.asyncReportQrcode();
        return flag?AjaxResult.success():AjaxResult.error();
    }

    @Log(title = "一键修改码进单状态", businessType = BusinessType.UPDATE)
    @ApiOperation("一键修改码进单状态")
    @PreAuthorize("@ss.hasPermi('system:merchantQrcode:update')")
    @GetMapping("updateEnterOrder")
    public AjaxResult updateEnterOrder(@RequestParam("status")Integer status) {
        List<MerchantQrcodeEntity> list = merchantQrcodeService.list(
                Wrappers.lambdaQuery(MerchantQrcodeEntity.class)
                        .eq(MerchantQrcodeEntity::getMerchantId, SecurityUtils.getUserId())
        );
        if (list != null && !list.isEmpty()) {
            list.forEach(data ->{
                data.setStatus(status);
            });
            boolean flag = merchantQrcodeService.updateBatchById(list);
            asyncRedisService.asyncReportQrcode();
            return flag ? AjaxResult.success(): AjaxResult.error();
        }

        return AjaxResult.error();
    }

    @ApiOperation("查询是否需要账号")
    @GetMapping("needAccountNumber")
    public R<AgentChannelEntity> needAccountNumber(@RequestParam("channelId")Long channelId) {
        MerchantEntity merchant = merchantService.getById(SecurityUtils.getUserId());
        AgentChannelEntity agentChannelEntity = agentChannelService.getOne(Wrappers.<AgentChannelEntity>lambdaQuery()
                .eq(AgentChannelEntity::getAgentId, merchant.getAgentId())
                .eq(AgentChannelEntity::getChannelId, channelId));
        return R.ok(agentChannelEntity);
    }
}
