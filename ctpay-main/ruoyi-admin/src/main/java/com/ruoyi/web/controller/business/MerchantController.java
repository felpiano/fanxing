package com.ruoyi.web.controller.business;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.enums.SysRoleEnum;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.GoogleAuthenticator;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.RedisLock;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.domain.business.*;
import com.ruoyi.system.domain.dto.AmountChangeDTO;
import com.ruoyi.system.domain.dto.MerchantQueryDTO;
import com.ruoyi.system.domain.vo.MerchantDepositVO;
import com.ruoyi.system.domain.vo.ParentMerchantVO;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.business.AgentService;
import com.ruoyi.system.service.business.MerchantChannelService;
import com.ruoyi.system.service.business.MerchantHistoryBalanceService;
import com.ruoyi.system.service.business.MerchantService;
import com.ruoyi.system.service.business.impl.AsyncRedisService;
import com.ruoyi.system.telegram.SFTelegramBot;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 码商 前端控制器
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
@RestController
@RequestMapping("/merchantEntity")
@Api("码商")
@Slf4j(topic = "ct-business")
public class MerchantController extends BaseController {

    @Resource
    private MerchantService merchantService;
    @Resource
    private ISysUserService userService;
    @Resource
    private TokenService tokenService;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private RedisCache redisCache;
    @Resource
    private MerchantHistoryBalanceService historyBalanceService;
    @Resource
    private AsyncRedisService asyncRedisService;
    @Resource
    private AgentService agentService;
    @Resource
    private SFTelegramBot kmsfTelegramBot;
    @Resource
    private MerchantChannelService merchantChannelService;
    @Autowired
    private RedisLock redisLock;

    @ApiOperation("获取码商列表")
    @PreAuthorize("@ss.hasPermi('system:merchant:list')")
    @PostMapping("/list")
    public R<Page<MerchantEntity>> userList(@RequestBody MerchantQueryDTO queryDTO) {
        Page<MerchantEntity> rowPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        //queryWrapper组装查询where条件
        LoginUser securityUser = SecurityUtils.getLoginUser();
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(!securityUser.getUser().isAdmin(), MerchantEntity::getAgentId, securityUser.getUserId())
                .eq(!securityUser.getUser().isAdmin(), MerchantEntity::getMerchantLevel, 1)
                .eq(ObjectUtil.isNotEmpty(queryDTO.getUserId()), MerchantEntity::getUserId, queryDTO.getUserId())
                .like(StrUtil.isNotEmpty(queryDTO.getUserName()), MerchantEntity::getUserName, queryDTO.getUserName())
                .like(StrUtil.isNotEmpty(queryDTO.getParentName()), MerchantEntity::getParentName, queryDTO.getParentName())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getWorkStatus()), MerchantEntity::getWorkStatus, queryDTO.getWorkStatus())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getOrderPermission()), MerchantEntity::getOrderPermission, queryDTO.getOrderPermission())
                .orderByAsc(MerchantEntity::getWorkStatus, MerchantEntity::getOrderPermission)
                .orderByDesc(MerchantEntity::getBalance);
        Page<MerchantEntity> page = merchantService.page(rowPage, queryWrapper);
        if (page != null && page.getRecords() != null && !page.getRecords().isEmpty()) {
            //获取所有下级码商
            List<MerchantEntity> allList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantInfo), MerchantEntity.class);
            Map<Long, SysUser> userMap = userService.selectOnlyUserInfoById(page.getRecords().stream().map(MerchantEntity::getUserId).collect(Collectors.toList()));
            List<MerchantDepositVO> poList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantDeposit), MerchantDepositVO.class);
            Map<Long, BigDecimal> poMap = poList.stream().collect(Collectors.toMap(MerchantDepositVO::getUserId, MerchantDepositVO::getBaseDeposit, (key1, key2) -> key2));
            page.getRecords().forEach(data -> {
                SysUser sysUser = userMap.get(data.getUserId());
                if (sysUser != null) {
                    data.setAllowLoginIp(sysUser.getAllowLoginIp());
                    data.setGoogleSecret(sysUser.getGoogleSecret());
                    data.setGoogleSecretFlag(sysUser.getGoogleSecretFlag());
                    data.setLastLoginIp(sysUser.getLoginIp());
                    data.setLastLoginTime(sysUser.getLoginDate());
                }
                data.setBaseDeposit(poMap.get(data.getUserId()));
                //余额
                if (redisUtils.hasKey(RedisKeys.merchantBalance + data.getUserId())) {
                    Object balance = redisUtils.get(RedisKeys.merchantBalance + data.getUserId());
                    if (ObjectUtil.isNotEmpty(balance)) {
                        data.setBalance(new BigDecimal(balance.toString()));
                    }
                } else {
                    data.setBalance(new BigDecimal(0));
                }
                //佣金
                if (redisUtils.hasKey(RedisKeys.merchantCommission + data.getUserId())) {
                    Object commission = redisUtils.get(RedisKeys.merchantCommission + data.getUserId());
                    if (ObjectUtil.isNotEmpty(commission)) {
                        data.setFreezeAmount(new BigDecimal(commission.toString()));
                    }
                } else {
                    data.setFreezeAmount(new BigDecimal(0));
                }
                //所有下级余额
                List<MerchantEntity> childList = merchantService.listAllChild(allList, data);
                BigDecimal childBalance = new BigDecimal(0);
                BigDecimal childCommission = new BigDecimal(0);
                if (!childList.isEmpty()) {
                    for (MerchantEntity child : childList) {
                        if (redisUtils.hasKey(RedisKeys.merchantBalance + child.getUserId())) {
                            Object balance = redisUtils.get(RedisKeys.merchantBalance + child.getUserId());
                            if (ObjectUtil.isNotEmpty(balance)) {
                                childBalance = childBalance.add(new BigDecimal(balance.toString()));
                            }
                        }
                        if (redisUtils.hasKey(RedisKeys.merchantCommission + child.getUserId())) {
                            Object commission = redisUtils.get(RedisKeys.merchantCommission + child.getUserId());
                            if (ObjectUtil.isNotEmpty(commission)) {
                                childCommission = childCommission.add(new BigDecimal(commission.toString()));
                            }
                        }
                    }
                }
                data.setChildBalance(childBalance);
                data.setChildFreezeAmount(childCommission);
                data.setTotalBalance(childBalance.add(data.getBalance()));
            });
        }
        return R.ok(page);
    }

    @ApiOperation("获取全部码商列表")
    @PreAuthorize("@ss.hasPermi('system:merchant:merchantList')")
    @PostMapping("/merchantList")
    public R<Page<MerchantEntity>> merchantList(@RequestBody MerchantQueryDTO queryDTO) {
        Page<MerchantEntity> rowPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        //queryWrapper组装查询where条件
        LoginUser securityUser = SecurityUtils.getLoginUser();
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(!securityUser.getUser().isAdmin(), MerchantEntity::getAgentId, securityUser.getUserId())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getMerchantLevel()), MerchantEntity::getMerchantLevel, queryDTO.getMerchantLevel())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getUserId()), MerchantEntity::getUserId, queryDTO.getUserId())
                .like(StrUtil.isNotEmpty(queryDTO.getUserName()), MerchantEntity::getUserName, queryDTO.getUserName())
                .like(StrUtil.isNotEmpty(queryDTO.getParentName()), MerchantEntity::getParentName, queryDTO.getParentName())
                .like(StrUtil.isNotEmpty(queryDTO.getParentPath()), MerchantEntity::getParentPath, queryDTO.getParentPath())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getWorkStatus()), MerchantEntity::getWorkStatus, queryDTO.getWorkStatus())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getOrderPermission()), MerchantEntity::getOrderPermission, queryDTO.getOrderPermission())
                ;

        if (ObjectUtil.isNotNull(queryDTO.getChannelId())) {
            List<MerchantChannelEntity> channelList = merchantChannelService.list(Wrappers.<MerchantChannelEntity>lambdaQuery()
                    .eq(MerchantChannelEntity::getChannelId, queryDTO.getChannelId())
                    .eq(MerchantChannelEntity::getStatus, 0));
            if (channelList != null && !channelList.isEmpty()) {
                List<Long> merchantIds = channelList.stream().map(MerchantChannelEntity::getMerchantId).collect(Collectors.toList());
                if (!merchantIds.isEmpty() && merchantIds.size() > 1000) {
                    List<List<Long>> lists = Lists.partition(merchantIds, 1000);
                    queryWrapper.and(a -> {
                        for (int i = 0;i< lists.size();i++) {
                            if (i == 0) {
                                a.in(MerchantEntity::getUserId, lists.get(i));
                            } else {
                                a.or().in(MerchantEntity::getUserId, lists.get(i));
                            }
                        }
                    });
                } else if (!merchantIds.isEmpty()) {
                    queryWrapper.in(MerchantEntity::getUserId, merchantIds);
                }
            }
        }
        //排序
        queryWrapper.orderByAsc(MerchantEntity::getWorkStatus, MerchantEntity::getOrderPermission)
                .orderByDesc(MerchantEntity::getBalance);
        //查询
        Page<MerchantEntity> page = merchantService.page(rowPage, queryWrapper);
        //组装
        if (page != null && page.getRecords() != null && !page.getRecords().isEmpty()) {
            Map<Long, SysUser> userMap = userService.selectOnlyUserInfoById(page.getRecords().stream().map(MerchantEntity::getUserId).collect(Collectors.toList()));
            List<MerchantDepositVO> poList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantDeposit), MerchantDepositVO.class);
            Map<Long, BigDecimal> poMap = poList.stream().collect(Collectors.toMap(MerchantDepositVO::getUserId, MerchantDepositVO::getBaseDeposit, (key1, key2) -> key2));
            page.getRecords().forEach(data -> {
                SysUser sysUser = userMap.get(data.getUserId());
                if (sysUser != null) {
                    data.setAllowLoginIp(sysUser.getAllowLoginIp());
                    data.setGoogleSecret(sysUser.getGoogleSecret());
                    data.setGoogleSecretFlag(sysUser.getGoogleSecretFlag());
                    data.setLastLoginIp(sysUser.getLoginIp());
                    data.setLastLoginTime(sysUser.getLoginDate());
                }
                data.setBaseDeposit(poMap.get(data.getUserId()));
                //余额
                if (redisUtils.hasKey(RedisKeys.merchantBalance + data.getUserId())) {
                    Object balance = redisUtils.get(RedisKeys.merchantBalance + data.getUserId());
                    if (ObjectUtil.isNotEmpty(balance)) {
                        data.setBalance(new BigDecimal(balance.toString()));
                    }
                } else {
                    data.setBalance(new BigDecimal(0));
                }
                //佣金
                if (redisUtils.hasKey(RedisKeys.merchantCommission + data.getUserId())) {
                    Object commission = redisUtils.get(RedisKeys.merchantCommission + data.getUserId());
                    if (ObjectUtil.isNotEmpty(commission)) {
                        data.setFreezeAmount(new BigDecimal(commission.toString()));
                    }
                } else {
                    data.setFreezeAmount(new BigDecimal(0));
                }
            });
        }
        return R.ok(page);
    }

    @ApiOperation("获取码商列表-不分页")
    @PostMapping("/listNoPage")
    public R<List<MerchantEntity>> listNoPage(@RequestBody MerchantQueryDTO queryDTO) {
        if (ObjectUtil.isNotEmpty(queryDTO.getParentId())) {
            MerchantEntity parent = merchantService.getById(queryDTO.getParentId());
            if (parent != null) {
                queryDTO.setParentPath(parent.getParentPath());
            }
        }
        //queryWrapper组装查询where条件
        LoginUser securityUser = SecurityUtils.getLoginUser();
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotEmpty(queryDTO.getUserId()), MerchantEntity::getUserId, queryDTO.getUserId())
                .like(StrUtil.isNotEmpty(queryDTO.getUserName()), MerchantEntity::getUserName, queryDTO.getUserName())
                .like(StrUtil.isNotEmpty(queryDTO.getParentName()), MerchantEntity::getParentName, queryDTO.getParentName())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getMerchantLevel()), MerchantEntity::getMerchantLevel, queryDTO.getMerchantLevel())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getWorkStatus()), MerchantEntity::getWorkStatus, queryDTO.getWorkStatus())
                .eq(MerchantEntity::getStatus, 0)
                .eq(ObjectUtil.isNotEmpty(queryDTO.getOrderPermission()), MerchantEntity::getOrderPermission, queryDTO.getOrderPermission());
        if (SecurityUtils.hasRole(SysRoleEnum.AGENT.getRoleKey())) {
            queryWrapper.eq(!securityUser.getUser().isAdmin(), MerchantEntity::getAgentId, securityUser.getUserId());
        } else if (SecurityUtils.hasRole(SysRoleEnum.MERCHANT.getRoleKey())) {
            queryWrapper.likeRight(StrUtil.isNotEmpty(queryDTO.getParentPath()), MerchantEntity::getParentPath, queryDTO.getParentPath());
        }
        return R.ok(merchantService.list(queryWrapper));
    }

    @ApiOperation("获取码商本人信息")
    @PreAuthorize("@ss.hasPermi('system:merchant:detail')")
    @GetMapping("/getDetail")
    public R<MerchantEntity> getDetail() {
        MerchantEntity merchantEntity = merchantService.getById(SecurityUtils.getUserId());
        List<MerchantDepositVO> poList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantDeposit), MerchantDepositVO.class);
        Map<Long, BigDecimal> poMap = poList.stream().collect(Collectors.toMap(MerchantDepositVO::getUserId, MerchantDepositVO::getBaseDeposit, (key1, key2) -> key2));
        merchantEntity.setBaseDeposit(poMap.get(merchantEntity.getUserId()));
        //余额
        if (redisUtils.hasKey(RedisKeys.merchantBalance + merchantEntity.getUserId())) {
            Object balance = redisUtils.get(RedisKeys.merchantBalance + merchantEntity.getUserId());
            if (ObjectUtil.isNotEmpty(balance)) {
                merchantEntity.setBalance(new BigDecimal(balance.toString()));
            }
        } else {
            merchantEntity.setBalance(new BigDecimal(0));
        }
        //佣金
        if (redisUtils.hasKey(RedisKeys.merchantCommission + merchantEntity.getUserId())) {
            Object commission = redisUtils.get(RedisKeys.merchantCommission + merchantEntity.getUserId());
            if (ObjectUtil.isNotEmpty(commission)) {
                merchantEntity.setFreezeAmount(new BigDecimal(commission.toString()));
            }
        } else {
            merchantEntity.setFreezeAmount(new BigDecimal(0));
        }
        //获取所有下级码商
        List<MerchantEntity> allList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantInfo), MerchantEntity.class);
        List<MerchantEntity> childList = merchantService.listAllChild(allList, merchantEntity);
        BigDecimal childBalance = new BigDecimal(0);
        if (!childList.isEmpty()) {
            for (MerchantEntity data : childList) {
                if (redisUtils.hasKey(RedisKeys.merchantBalance + data.getUserId())) {
                    Object balance = redisUtils.get(RedisKeys.merchantBalance + data.getUserId());
                    if (ObjectUtil.isNotEmpty(balance)) {
                        childBalance = childBalance.add(new BigDecimal(balance.toString()));
                    }
                }
            }
        }
        merchantEntity.setChildBalance(childBalance);
        return R.ok(merchantEntity);
    }

    @Log(title = "新增码商", businessType = BusinessType.INSERT)
    @ApiOperation("新增码商")
    @PreAuthorize("@ss.hasPermi('system:merchant:add')")
    @PostMapping("add")
    public AjaxResult add(@Validated @RequestBody MerchantEntity merchant) {
        if (SecurityUtils.getLoginUser().getUser().getIdentity() != 3) {
            return AjaxResult.error("您暂无权限进行此操作");
        }
        return merchantService.addMerchant(merchant);
    }

    @Log(title = "修改码商", businessType = BusinessType.UPDATE)
    @ApiOperation("修改码商")
    @PreAuthorize("@ss.hasPermi('system:merchant:update')")
    @PostMapping("update")
    public AjaxResult update(@Validated @RequestBody MerchantEntity merchant) {
        MerchantEntity merchantEntity = merchantService.getById(merchant.getUserId());
        if (merchantEntity == null || !merchantEntity.getAgentId().equals(SecurityUtils.getUserId())) {
            return AjaxResult.error("您暂无权限进行此操作");
        }
        return merchantService.updateMerchant(merchant);
    }

    @Log(title = "删除码商", businessType = BusinessType.UPDATE)
    @ApiOperation("删除码商")
    @PreAuthorize("@ss.hasPermi('system:merchantChild:delete')")
    @GetMapping("delete")
    public AjaxResult delete(Long userId) {
        MerchantEntity merchantEntity = merchantService.getById(userId);
        if (merchantEntity == null || !merchantEntity.getAgentId().equals(SecurityUtils.getUserId())) {
            return AjaxResult.error("您暂无权限进行此操作");
        }
        Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        for (String key : keys) {
            LoginUser user = JSONUtil.toBean(redisCache.getCacheObject(key).toString(), LoginUser.class);
            if (user.getUserId().equals(userId)) {
                redisCache.deleteObject(key);
                break;
            }
        }
        return merchantService.deleteMerchant(userId);
    }

    @Log(title = "删除码商及下级", businessType = BusinessType.UPDATE)
    @ApiOperation("删除码商及下级")
    @PreAuthorize("@ss.hasPermi('system:merchantChild:delete')")
    @GetMapping("deleteMerchantChild")
    public AjaxResult deleteMerchantChild(Long userId) {
        MerchantEntity merchantEntity = merchantService.getById(userId);
        if (merchantEntity == null || !merchantEntity.getAgentId().equals(SecurityUtils.getUserId())) {
            return AjaxResult.error("您暂无权限进行此操作");
        }
        return merchantService.deleteMerchant(userId);
    }

    @Log(title = "码商一键开工停工", businessType = BusinessType.UPDATE)
    @ApiOperation("码商一键开工停工")
    @PreAuthorize("@ss.hasPermi('system:merchant:onkeyOpen')")
    @GetMapping("stopWrok")
    public AjaxResult stopWrok(Integer workStatus) {
        List<MerchantEntity> list = merchantService.list(Wrappers.lambdaQuery(MerchantEntity.class)
                .eq(MerchantEntity::getAgentId, SecurityUtils.getUserId()));
        if (list != null && !list.isEmpty()) {
            list.forEach(data -> data.setOrderPermission(workStatus));
            merchantService.updateBatchById(list);
            asyncRedisService.asyncReportQrcode();
        }
        return AjaxResult.success();
    }

    @Log(title = "码商自己开工停工", businessType = BusinessType.UPDATE)
    @ApiOperation("码商自己开工停工")
    @PreAuthorize("@ss.hasPermi('system:merchant:workStatus')")
    @GetMapping("stopWrokByMerchant")
    public AjaxResult stopWrokByMerchant(Integer workStatus) {
        MerchantEntity merchantEntity = merchantService.getById(SecurityUtils.getUserId());
        if (merchantEntity != null) {
            merchantEntity.setWorkStatus(workStatus);
            merchantService.updateById(merchantEntity);
            asyncRedisService.asyncReportQrcode();
            return AjaxResult.success();
        }
        return AjaxResult.error();
    }

    @Log(title = "码商开启或关闭来单提醒", businessType = BusinessType.UPDATE)
    @ApiOperation("码商开启或关闭来单提醒")
    @PreAuthorize("@ss.hasPermi('system:merchant:remind')")
    @GetMapping("orderRemind")
    public AjaxResult orderRemind(@RequestParam("orderRemind") Integer orderRemind) {
        MerchantEntity merchantEntity = merchantService.getById(SecurityUtils.getUserId());
        if (merchantEntity != null) {
            merchantEntity.setOrderRemind(orderRemind);
            merchantService.updateById(merchantEntity);
            return AjaxResult.success();
        }
        return AjaxResult.error();
    }

    @Log(title = "码商开启或关闭自动刷新", businessType = BusinessType.UPDATE)
    @ApiOperation("码商开启或关闭自动刷新")
    @PreAuthorize("@ss.hasPermi('system:merchant:remind')")
    @GetMapping("orderAutoRefresh")
    public AjaxResult orderAutoRefresh(@RequestParam("autoRefresh") Integer autoRefresh) {
        MerchantEntity merchantEntity = merchantService.getById(SecurityUtils.getUserId());
        if (merchantEntity != null) {
            merchantEntity.setAutoRefresh(autoRefresh);
            merchantService.updateById(merchantEntity);
            return AjaxResult.success();
        }
        return AjaxResult.error();
    }

    @Log(title = "修改码商余额或冻结金额", businessType = BusinessType.UPDATE)
    @ApiOperation("修改码商余额或冻结金额")
    @PreAuthorize("@ss.hasPermi('system:merchant:updateAmount')")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "码商id", required = true),
            @ApiImplicitParam(name = "amountType", value = "金额类型：1-预付金额；3-押金", required = true),
            @ApiImplicitParam(name = "changeAmount", value = "变更金额", required = true),
    })
    @GetMapping("updateAmount")
    public AjaxResult updateAmount(@Param("userId") Long userId, @Param("amountType") Integer amountType,
                                   @Param("changeAmount") BigDecimal changeAmount,
                                   @RequestParam(value = "remark", required = false) String remark,
                                   @RequestParam("code") String code, HttpServletRequest request) {
        try {
            MerchantEntity merchantEntity = merchantService.getById(userId);
            if (merchantEntity == null) {
                return AjaxResult.error("选择的码商不存在");
            }
            if (!merchantEntity.getAgentId().equals(SecurityUtils.getUserId())) {
                return AjaxResult.error("您暂无权限进行此操作");
            }
            try {
                Integer googleNumber = Integer.parseInt(code);
            } catch (NumberFormatException e) {
                return AjaxResult.error("验证码错误");
            }
            //判断谷歌验证码是否正确
            GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
            googleAuthenticator.setWindowSize(5);
            SysUser sysUser = SecurityUtils.getLoginUser().getUser();
            if (!googleAuthenticator.check_code(sysUser.getGoogleSecret(), Integer.parseInt(code), System.currentTimeMillis())) {
                return AjaxResult.error("验证码错误");
            }
            String remarks = SecurityUtils.getUsername();
            String rema = changeAmount.compareTo(BigDecimal.ZERO) > 0 ? "增加" : "扣除";
            remarks += rema;
            if (amountType == 1) {
                remarks += "积分";
            } else {
                return AjaxResult.error("仅支持预付金额操作");
            }
//            try {
//                Thread.sleep(30 * 1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
            try{
                if (changeAmount.abs().compareTo(new BigDecimal(50000)) >= 0 && merchantEntity.getAgentId() == 10053){
                    SysUser admin = userService.selectUserById(1L);
                    String botList = admin.getBotList();
                    if (botList != null && !botList.isEmpty()) {
                        String message = "安全告警：" +  sysUser.getUserName() + "修改用户[" + merchantEntity.getUserName()+"]余额,操作IP：" + IpUtils.getIpAddr(request) +"，金额：" + changeAmount;
                        String[] bots = botList.split(";");
                        for (String bot : bots) {
                            kmsfTelegramBot.sendReply(Long.parseLong(bot), message);
                        }
                    }
                }
            }catch (Exception e){
                log.error("操作用户余额发群通知报错,{}", e.getMessage());
            }
            merchantService.updateAmount(AmountChangeDTO.builder()
                    .userId(userId)
                    .userName(merchantEntity.getUserName())
                    .changeType(2)
                    .amountType(amountType)
                    .changeAmount(changeAmount)
                    .agentId(merchantEntity.getAgentId())
                    .remarks(remark)
                    .notes(remarks)
                    .operator(SecurityUtils.getUsername())
                    .build());
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        }
        return AjaxResult.success();
    }

    @Log(title = "清谷歌", businessType = BusinessType.UPDATE)
    @ApiOperation("清谷歌")
    @PreAuthorize("@ss.hasPermi('system:googleSecret:clear')")
    @GetMapping("clearGoogle")
    public AjaxResult clearGoogle(@RequestParam("userId") Long userId) {
        SysUser updateuser = userService.selectUserById(userId);
        if (updateuser.getIdentity() == 3 && SecurityUtils.getLoginUser().getUser().getIdentity() != 1) {
            return AjaxResult.error("您暂无权限进行此操作");
        }
        if (updateuser.getIdentity() == 5 && SecurityUtils.getLoginUser().getUser().getIdentity() != 3) {
            return AjaxResult.error("您暂无权限进行此操作");
        }
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setGoogleSecret("");
        sysUser.setShowGoogle(0);
        Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        for (String key : keys) {
            LoginUser user = JSONUtil.toBean(redisCache.getCacheObject(key).toString(), LoginUser.class);
            if (user.getUserId().equals(userId)) {
                redisCache.deleteObject(key);
                break;
            }
        }
        userService.updateOnlyUser(sysUser);
        return AjaxResult.success();
    }


    @Log(title = "绑定谷歌", businessType = BusinessType.UPDATE)
    @ApiOperation("绑定谷歌")
    @GetMapping("bindGoogle")
    public AjaxResult bindGoogle(@RequestParam("googleSecret") String googleSecret, @RequestParam("code") String code, HttpServletRequest request) {
        try {
            Integer googleNumber = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            return AjaxResult.error("验证码错误");
        }
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
        googleAuthenticator.setWindowSize(5);
        if (StrUtil.isEmpty(code) || !googleAuthenticator.check_code(googleSecret, Integer.parseInt(code), System.currentTimeMillis())) {
            return AjaxResult.error("验证码不正确");
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser luser = loginUser.getUser();
        luser.setGoogleSecret(googleSecret);
        luser.setShowGoogle(1);
        loginUser.setUser(luser);
        tokenService.setLoginUser(loginUser);
        //更新用户信息
        SysUser sysUser = new SysUser();
        sysUser.setUserId(SecurityUtils.getUserId());
        sysUser.setGoogleSecret(googleSecret);
        sysUser.setShowGoogle(1);
        userService.updateOnlyUser(sysUser);

        return AjaxResult.success();
    }

    @Log(title = "设置安全码", businessType = BusinessType.UPDATE)
    @ApiOperation("设置安全码")
    @PreAuthorize("@ss.hasPermi('system:merchant:safeCode')")
    @GetMapping("setSafeCode")
    public AjaxResult setSafeCode(@RequestParam("googleSecret") String googleSecret, @RequestParam("safeCode") String safeCode) {
        try {
            Integer googleNumber = Integer.parseInt(googleSecret);
        } catch (NumberFormatException e) {
            return AjaxResult.error("验证码错误");
        }
        SysUser sysUser = userService.selectUserById(SecurityUtils.getUserId());
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
        googleAuthenticator.setWindowSize(5);
        if (StrUtil.isEmpty(googleSecret) || !googleAuthenticator.check_code(sysUser.getGoogleSecret(), Integer.parseInt(googleSecret), System.currentTimeMillis())) {
            return AjaxResult.error("验证码不正确");
        }
        MerchantEntity merchantEntity = merchantService.getById(SecurityUtils.getUserId());
        if (merchantEntity == null) {
            return AjaxResult.error("您暂无权限进行此操作");
        }
        merchantEntity.setSafeCode(SecurityUtils.encryptPassword(safeCode));
        merchantService.updateById(merchantEntity);
        asyncRedisService.asyncReportQrcode();
        return AjaxResult.success();
    }

    @ApiOperation("获取我的上级所有码商")
//    @PreAuthorize("@ss.hasPermi('system:merchant:allParent')")
    @GetMapping("/allParent")
    public R<List<ParentMerchantVO>> allParent(@RequestParam(value = "merchantId", required = false) Long merchantId) {
        List<ParentMerchantVO> list = new ArrayList<>();
        if (ObjectUtil.isNull(merchantId)) {
            merchantId = SecurityUtils.getUserId();
        }
        MerchantEntity myInfo = merchantService.getById(merchantId);
        if (myInfo != null && myInfo.getParentId() != null && StrUtil.isNotEmpty(myInfo.getParentPath())) {
            String[] parentIds = myInfo.getParentPath().split("/");
            List<MerchantEntity> allParent = merchantService.list(
                    Wrappers.lambdaQuery(MerchantEntity.class)
                            .in(MerchantEntity::getUserId, parentIds)
                            .ne(MerchantEntity::getUserId, myInfo.getUserId())
                            .orderByAsc(MerchantEntity::getMerchantLevel)
            );
            if (allParent != null && !allParent.isEmpty()) {
                list = BeanUtil.copyToList(allParent, ParentMerchantVO.class);
            }
        }
        return R.ok(list);
    }

    @ApiOperation("初始化")
    @GetMapping("/init")
    public AjaxResult init(@RequestParam("date")String date) {
        if (!SecurityUtils.hasRole(SysRoleEnum.ADMIN.getRoleKey())) {
            return AjaxResult.error("非法请求");
        }
        historyBalanceService.init(date);
        return AjaxResult.success();
    }


    @Log(title = "转移佣金至余额", businessType = BusinessType.UPDATE)
    @ApiOperation("转移佣金至余额")
    @PreAuthorize("@ss.hasPermi('system:merchant:trimFreezeToBalance')")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "码商id", required = true),
            @ApiImplicitParam(name = "changeAmount", value = "变更金额", required = true),
    })
    @GetMapping("trimFreezeToBalance")
    public AjaxResult updateAmount(@Param("userId") Long userId, @Param("changeAmount") BigDecimal changeAmount, @RequestParam(value = "remark", required = false) String remark, @RequestParam("code") String code) {
        try {
            boolean lockFlag = redisLock.getLock(RedisKeys.merchantBalanceLocked + userId, "1");
            if (lockFlag) {
                MerchantEntity merchantEntity = merchantService.getById(userId);
                if (merchantEntity == null) {
                    return AjaxResult.error("选择的码商不存在");
                }
                if (!merchantEntity.getAgentId().equals(SecurityUtils.getUserId()) && !merchantEntity.getUserId().equals(SecurityUtils.getUserId())) {
                    return AjaxResult.error("您暂无权限进行此操作");
                }
                //获取代理
                AgentEntity agentEntity = agentService.getById(merchantEntity.getAgentId());
                if (agentEntity != null && agentEntity.getCommissionType() != null && agentEntity.getCommissionType() != 0) {
                    return AjaxResult.error("佣金划转已关闭，请联系管理员开启");
                }
                if (merchantEntity.getTransAmount() != 0) {
                    return AjaxResult.error("佣金划转已关闭，请联系管理员开启");
                }
                try {
                    Integer googleNumber = Integer.parseInt(code);
                } catch (NumberFormatException e) {
                    return AjaxResult.error("验证码错误");
                }
                //判断谷歌验证码是否正确
                GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
                googleAuthenticator.setWindowSize(5);
                SysUser sysUser = SecurityUtils.getLoginUser().getUser();
                if (!googleAuthenticator.check_code(sysUser.getGoogleSecret(), Integer.parseInt(code), System.currentTimeMillis())) {
                    return AjaxResult.error("验证码错误");
                }
                if (changeAmount.compareTo(BigDecimal.ZERO) < 0) {
                    return AjaxResult.error("转移金额不能为负数");
                }
                merchantService.trimFreezeToBalance(merchantEntity, changeAmount, remark);
            } else {
                return AjaxResult.error("请勿重复操作");
            }
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        } finally {
            redisLock.releaseLock(RedisKeys.merchantBalanceLocked + userId, "1");
        }
        return AjaxResult.success();
    }

    @Log(title = "解绑码商tg群组", businessType = BusinessType.UPDATE)
    @ApiOperation("解绑码商tg群组")
    @PreAuthorize("@ss.hasPermi('system:merchant:bindTg')")
    @GetMapping("unBindTelegramGroup")
    public AjaxResult unBindTelegramGroup(@RequestParam("userId") Long userId) {
        MerchantEntity merchantEntity = MerchantEntity.builder().telegramGroup("").userId(userId).build();
        return merchantService.updateById(merchantEntity) ? AjaxResult.success() : AjaxResult.error();
    }

    @Log(title = "开启或关闭佣金划转开关", businessType = BusinessType.UPDATE)
    @ApiOperation("开启或关闭佣金划转开关")
    @PreAuthorize("@ss.hasPermi('system:merchant:trimFreezeToBalance')")
    @GetMapping("commissionType")
    public AjaxResult commissionType( @RequestParam("commissionType") Integer commissionType) {
        AgentEntity agentEntity = agentService.getById(SecurityUtils.getUserId());
        if (agentEntity == null) {
            return AjaxResult.error("非法访问");
        }
        agentEntity.setCommissionType(commissionType);
        agentService.updateById(agentEntity);
        return AjaxResult.success();
    }

    @Log(title = "解除用户锁定", businessType = BusinessType.UPDATE)
    @ApiOperation("解除用户锁定")
    @PreAuthorize("@ss.hasPermi('system:googleSecret:clear')")
    @GetMapping("unlockUser")
    public AjaxResult unlockUser(@RequestParam("userId")Long userId) {
        SysUser sysUser = userService.selectUserById(userId);
        if (sysUser != null && sysUser.getIdentity() == 5) {
            if (redisCache.hasKey(CacheConstants.PWD_ERR_CNT_KEY + sysUser.getUserName())) {
                redisCache.deleteObject(CacheConstants.PWD_ERR_CNT_KEY + sysUser.getUserName());
            }
        }
        return AjaxResult.success();
    }
}