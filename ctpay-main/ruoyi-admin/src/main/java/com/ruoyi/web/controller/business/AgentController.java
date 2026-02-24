package com.ruoyi.web.controller.business;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.enums.SysRoleEnum;
import com.ruoyi.common.utils.GoogleAuthenticator;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.framework.web.domain.server.Sys;
import com.ruoyi.system.domain.business.*;
import com.ruoyi.system.domain.dto.AgentQueryDTO;
import com.ruoyi.system.domain.dto.AmountChangeDTO;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.business.*;
import com.ruoyi.system.service.business.impl.AsyncRedisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 代理基础信息表 前端控制器
 * </p>
 *
 * @author admin
 * @since 2024-09-08
 */
@RestController
@RequestMapping("/agentEntity")
@Api("代理管理")
public class AgentController extends BaseController {
    @Resource
    private AgentService agentService;
    @Resource
    private AgentChannelService agentChannelService;
    @Resource
    private ISysUserService userService;
    @Resource
    private BaseChannelService baseChannelService;
    @Autowired
    private RedisUtils redisUtils;
    @Resource
    private RedisCache redisCache;
    @Resource
    private ChannelService channelService;
    @Resource
    private AsyncRedisService asyncRedisService;
    @Resource
    private MerchantService merchantService;

    @ApiOperation("获取代理列表")
    @PreAuthorize("@ss.hasPermi('system:agent:list')")
    @PostMapping("/list")
    public R<Page<AgentEntity>> userList(@RequestBody AgentQueryDTO queryDTO) {
        Page<AgentEntity> rowPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        //queryWrapper组装查询where条件
        LambdaQueryWrapper<AgentEntity> queryWrapper = Wrappers.lambdaQuery(AgentEntity.class).like(StrUtil.isNotEmpty(queryDTO.getUserName()), AgentEntity::getUserName, queryDTO.getUserName())
                .like(StrUtil.isNotEmpty(queryDTO.getNickName()), AgentEntity::getNickName, queryDTO.getNickName())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getStatus()), AgentEntity::getStatus, queryDTO.getStatus())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getUserId()), AgentEntity::getUserId, queryDTO.getUserId())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getDelFlag()), AgentEntity::getDelFlag, queryDTO.getDelFlag());
        if (!SecurityUtils.isAdmin(SecurityUtils.getLoginUser().getUser().getRoles())) {
            queryWrapper.eq(AgentEntity::getUserId, SecurityUtils.getLoginUser().getUser().getUserId());
        }
        Page<AgentEntity> page = agentService.page(rowPage, queryWrapper);
        if (page != null && page.getRecords() != null && !page.getRecords().isEmpty()) {
            Map<Long, SysUser> userMap = userService.selectOnlyUserInfoById(page.getRecords().stream().map(AgentEntity::getUserId).collect(Collectors.toList()));
            page.getRecords().forEach(agentEntity -> {
                SysUser sysUser = userMap.get(agentEntity.getUserId());
                if (sysUser != null) {
                    agentEntity.setAllowLoginIp(sysUser.getAllowLoginIp());
                    agentEntity.setGoogleSecret(sysUser.getGoogleSecret());
                    agentEntity.setGoogleSecretFlag(sysUser.getGoogleSecretFlag());
                    agentEntity.setLastLoginIp(sysUser.getLoginIp());
                    agentEntity.setLastLoginTime(sysUser.getLoginDate());
                    agentEntity.setUid(sysUser.getUid());
                }
                if (redisUtils.hasKey(RedisKeys.agentBalance + agentEntity.getUserId())) {
                    Object balance = redisUtils.get(RedisKeys.agentBalance + agentEntity.getUserId());
                    if (ObjectUtil.isNotEmpty(balance)) {
                        agentEntity.setBalance(new BigDecimal(balance.toString()));
                    }
                }else {
                    agentEntity.setBalance(new BigDecimal(0));
                }
            });

        }
        return R.ok(page);
    }

    @ApiOperation("获取代理不分页列表")
    @PreAuthorize("@ss.hasPermi('system:agent:list')")
    @PostMapping("/listNoPage")
    public R<List<AgentEntity>> listNoPage(@RequestBody AgentQueryDTO queryDTO) {
        //queryWrapper组装查询where条件
        LambdaQueryWrapper<AgentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotEmpty(queryDTO.getUserName()), AgentEntity::getUserName, queryDTO.getUserName())
                .like(StrUtil.isNotEmpty(queryDTO.getNickName()), AgentEntity::getNickName, queryDTO.getNickName())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getStatus()), AgentEntity::getStatus, queryDTO.getStatus())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getUserId()), AgentEntity::getUserId, queryDTO.getUserId())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getDelFlag()), AgentEntity::getDelFlag, queryDTO.getDelFlag());
        if (!SecurityUtils.isAdmin(SecurityUtils.getLoginUser().getUser().getRoles())) {
            queryWrapper.eq(AgentEntity::getUserId, SecurityUtils.getLoginUser().getUser().getUserId());
        }
        queryWrapper.select(AgentEntity::getUserId, AgentEntity::getNickName, AgentEntity::getUserName);
        return R.ok(agentService.list(queryWrapper));
    }

    @ApiOperation("代理获取自身信息")
    @PreAuthorize("@ss.hasPermi('system:agent:detail')")
    @GetMapping("getDetail")
    public R<AgentEntity> getDetail() {
        AgentEntity agentEntity = agentService.getById(SecurityUtils.getUserId());
        if (redisUtils.hasKey(RedisKeys.agentBalance + agentEntity.getUserId())) {
            Object balance = redisUtils.get(RedisKeys.agentBalance + agentEntity.getUserId());
            if (ObjectUtil.isNotEmpty(balance)) {
                agentEntity.setBalance(new BigDecimal(balance.toString()));
            }
        }else {
            agentEntity.setBalance(new BigDecimal(0));
        }
        return R.ok(agentEntity);
    }

    @Log(title = "新增代理", businessType = BusinessType.INSERT)
    @ApiOperation("新增代理")
    @PreAuthorize("@ss.hasPermi('system:agent:add')")
    @PostMapping("add")
    public AjaxResult add(@Validated @RequestBody SysUser user) {
        if (!SecurityUtils.isAdmin(SecurityUtils.getLoginUser().getUser().getRoles())) {
            return AjaxResult.error("您没有权限操作");
        }
        return agentService.saveAgent(user);
    }

    @Log(title = "修改代理", businessType = BusinessType.UPDATE)
    @ApiOperation("修改代理")
    @PreAuthorize("@ss.hasPermi('system:agent:update')")
    @PostMapping("update")
    public AjaxResult update(@Validated @RequestBody AgentEntity agentEntity) {
        return agentService.updateAgent(agentEntity);
    }

    @Log(title = "删除代理", businessType = BusinessType.DELETE)
    @ApiOperation("删除代理")
    @PreAuthorize("@ss.hasPermi('system:agent:delete')")
    @GetMapping("deleteAgent")
    public AjaxResult deleteAgent(@RequestParam("userId")Long userId) {
        if (!SecurityUtils.isAdmin(SecurityUtils.getLoginUser().getUser().getRoles())) {
            return AjaxResult.error("您没有权限操作");
        }
        return agentService.deleteAgent(userId);
    }

    @Log(title = "修改代理状态", businessType = BusinessType.UPDATE)
    @ApiOperation("修改代理状态")
    @PreAuthorize("@ss.hasPermi('system:agent:updateStatus')")
    @GetMapping("upateStatus")
    public AjaxResult upateStatus(@RequestParam("userId")Long userId, @RequestParam("status")Integer status) {
        if (!SecurityUtils.isAdmin(SecurityUtils.getLoginUser().getUser().getRoles())) {
            return AjaxResult.error("您没有权限操作");
        }
        AgentEntity agentEntity = AgentEntity.builder().status(status).userId(userId).build();
        boolean flag = agentService.updateById(agentEntity);
        asyncRedisService.asyncReportQrcode();
        return flag?AjaxResult.success():AjaxResult.error();
    }

    @Log(title = "充值余额", businessType = BusinessType.UPDATE)
    @ApiOperation("充值余额")
    @PreAuthorize("@ss.hasPermi('system:agent:saveBalance')")
    @GetMapping("chargeAmount")
    public AjaxResult chargeAmount(@RequestParam("userId")Long userId, @RequestParam("amount") BigDecimal amount, @RequestParam("code")String code) {
        if (!SecurityUtils.isAdmin(SecurityUtils.getLoginUser().getUser().getRoles())) {
            return AjaxResult.error("您没有权限操作");
        }
        try {
            Integer googleNumber = Integer.parseInt(code);
        }catch (NumberFormatException e) {
            return AjaxResult.error("验证码错误");
        }
        //判断谷歌验证码是否正确
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
        googleAuthenticator.setWindowSize(5);
        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        if (!googleAuthenticator.check_code(sysUser.getGoogleSecret(), Integer.parseInt(code), System.currentTimeMillis())) {
            return AjaxResult.error("验证码错误");
        }
        AgentEntity agentEntity = agentService.getById(userId);
        return agentService.updateAgentBalance(AmountChangeDTO.builder()
                        .userId(userId)
                        .userName(agentEntity.getUserName())
                        .changeAmount(amount)
                        .amountType(1)
                        .changeType(2)
                        .notes("充值").build());
    }


    @ApiOperation("产品")
    @PreAuthorize("@ss.hasPermi('system:agent:product')")
    @ApiParam(name = "agentId", value = "代理ID", required = true)
    @GetMapping("listAgentChannel")
    public R listAgentChannel(
            @RequestParam(value = "agentId", required = false)Long agentId,
            @RequestParam(value = "channelCode", required = false)String channelCode,
            @RequestParam(value = "channelName", required = false)String channelName
            ) {
        if (ObjectUtil.isNull(agentId)) {
            SysUser sysUser = SecurityUtils.getLoginUser().getUser();
            if (sysUser.getIdentity() == 3) {
                agentId = SecurityUtils.getLoginUser().getUser().getUserId();
            }  else if (sysUser.getIdentity() != 1) {
                return R.ok(Collections.emptyList());
            } else if (sysUser.getIdentity() == 1) {
                return R.ok(baseChannelService.listByUser(agentId, channelCode, channelName));
            }
        }
        List<AgentChannelEntity> list = agentChannelService.listByAgentId(agentId, channelCode, channelName);
        return R.ok(list);
    }

    @Log(title = "修改产品", businessType = BusinessType.UPDATE)
    @ApiOperation("修改产品")
    @PreAuthorize("@ss.hasPermi('system:agent:saveProduct')")
    @PostMapping("updateAgentChannel")
    public AjaxResult updateBatch(@RequestBody List<AgentChannelEntity> agentChannelEntities) {
        if (SecurityUtils.getLoginUser().getUser().getIdentity() == 3) {
            agentChannelService.updateBatchById(agentChannelEntities);

        } else if (SecurityUtils.getLoginUser().getUser().getIdentity() == 1) {
            List<BaseChannelEntity> list = BeanUtil.copyToList(agentChannelEntities, BaseChannelEntity.class);
            channelService.updateBaseChannel(list.get(0));
        }
        asyncRedisService.asyncReportQrcode();
        return AjaxResult.success();
    }

    @Log(title = "管理员设置代理费率", businessType = BusinessType.UPDATE)
    @ApiOperation("管理员设置代理费率")
    @PreAuthorize("@ss.hasPermi('system:agent:saveAgentChannel')")
    @PostMapping("saveAgentChannel")
    public AjaxResult saveAgentChannel(@RequestBody List<AgentChannelEntity> agentChannelEntities) {
        if (SecurityUtils.getLoginUser().getUser().getIdentity() == 1) {
            agentChannelService.updateBatchById(agentChannelEntities);
            asyncRedisService.asyncReportQrcode();
        }
        return AjaxResult.success();
    }

    @ApiOperation("拉黑IP")
    @PreAuthorize("@ss.hasPermi('system:agent:putBlackClientIp')")
    @GetMapping("putBlackClientIp")
    public AjaxResult putBlackClientIp(@RequestParam("clientIp") String clientIp, @RequestParam("type")Integer type) {
        if (!SecurityUtils.hasRole(SysRoleEnum.AGENT.getRoleKey())) {
            return AjaxResult.error("您没有权限操作");
        } else {
            if (type == 1) {
                if(!redisCache.hasKey(RedisKeys.clientIpList + clientIp)){
                    redisCache.setCacheObject(RedisKeys.clientIpList + clientIp, clientIp);
                }
            } else {
                redisCache.deleteObject(RedisKeys.clientIpList + clientIp);
            }
        }
        return AjaxResult.success();
    }

    @ApiOperation("黑名单IP列表")
    @PreAuthorize("@ss.hasPermi('system:agent:blackIpList')")
    @GetMapping("blackIpList")
    public R blackIpList() {
        Collection<String> list = redisCache.keys(RedisKeys.clientIpList + "*");
        List<String> resultList = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            list.forEach(data -> {
                resultList.add(redisCache.getCacheObject(data));
            });
        }
        return R.ok(resultList);
    }


    @Log(title = "一键清除1级码商以下的费率", businessType = BusinessType.UPDATE)
    @ApiOperation("一键清除1级码商以下的费率")
    @GetMapping("onekeyClearRate")
    public AjaxResult onekeyClearRate(@RequestParam("channelId")Long channelId) {
        if (!SecurityUtils.hasRole(SysRoleEnum.AGENT.getRoleKey())) {
            return AjaxResult.error("暂无权限");
        }
        return merchantService.onekeyClearRate(channelId);
    }
}
