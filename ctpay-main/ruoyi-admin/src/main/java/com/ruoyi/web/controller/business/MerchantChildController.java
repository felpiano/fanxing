package com.ruoyi.web.controller.business;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.enums.SysRoleEnum;
import com.ruoyi.common.utils.GoogleAuthenticator;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.business.MerchantChannelEntity;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.ruoyi.system.domain.business.MerchantQrcodeEntity;
import com.ruoyi.system.domain.dto.ChildMerchantChannelDTO;
import com.ruoyi.system.domain.dto.MerchantChildSaveDTO;
import com.ruoyi.system.domain.dto.MerchantQrcodeQueryDTO;
import com.ruoyi.system.domain.dto.MerchantQueryDTO;
import com.ruoyi.system.domain.vo.MerchantDepositVO;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.business.MerchantChannelService;
import com.ruoyi.system.service.business.MerchantChildService;
import com.ruoyi.system.service.business.MerchantQrcodeService;
import com.ruoyi.system.service.business.MerchantService;
import com.ruoyi.system.service.business.impl.AsyncRedisService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/merchantChild")
@Api("码商下级")
public class MerchantChildController {
    @Resource
    private MerchantChildService childService;
    @Resource
    private MerchantChannelService merchantChannelService;
    @Resource
    private ISysUserService userService;
    @Resource
    private MerchantService merchantService;
    @Autowired
    private RedisUtils redisUtils;
    @Resource
    private AsyncRedisService asyncRedisService;
    @Resource
    private MerchantQrcodeService merchantQrcodeService;

    @Log(title = "添加下级码商", businessType = BusinessType.INSERT)
    @ApiOperation("添加下级码商")
    @PreAuthorize("@ss.hasPermi('system:merchantChild:addChild')")
    @GetMapping("addChild")
    public AjaxResult addChild(@RequestParam("loginName") String loginName, @RequestParam("password") String password) {
        if (SecurityUtils.hasRole(SysRoleEnum.MERCHANT.getRoleKey())) {
            return childService.addChildMerchant(loginName, loginName, password);
        }
        return AjaxResult.error("您暂无权限添加下级");
    }

    @Log(title = "修改下级码商", businessType = BusinessType.UPDATE)
    @ApiOperation("修改下级码商")
    @PreAuthorize("@ss.hasPermi('system:merchantChild:updateChild')")
    @PostMapping("updateChild")
    public AjaxResult updateChild(@RequestBody MerchantChildSaveDTO saveDTO) {
        return childService.updateChildMerchant(saveDTO);
    }

    @Log(title = "一键关闭下级所有码商", businessType = BusinessType.UPDATE)
    @ApiOperation("一键关闭下级所有码商")
    @PreAuthorize("@ss.hasPermi('system:merchantChild:onkeyOff')")
    @GetMapping("onkeyOff")
    public AjaxResult onkeyOff(@RequestParam("orderPermission") Integer orderPermission) {
        MerchantEntity merchant = merchantService.getById(SecurityUtils.getUserId());
        List<MerchantEntity> allList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantInfo), MerchantEntity.class);
        List<MerchantEntity> childList = merchantService.listAllChild(allList, merchant);
        if (childList != null && !childList.isEmpty()) {
            childList.forEach(data -> {
                data.setOrderPermission(orderPermission);
            });
            merchantService.updateBatchById(childList);
            asyncRedisService.asyncReportQrcode();
        }
        return AjaxResult.success();
    }

    @Log(title = "一键关闭指定码商及下级", businessType = BusinessType.UPDATE)
    @ApiOperation("一键关闭指定码商及下级")
    @PreAuthorize("@ss.hasPermi('system:merchantChild:onkeyOffByMerchant')")
    @GetMapping("onkeyOffByMerchant")
    public AjaxResult onkeyOffByMerchant(@RequestParam("merchantId") Long merchantId, @RequestParam("orderPermission") Integer orderPermission) {
        MerchantEntity merchant = merchantService.getById(merchantId);
        List<MerchantEntity> allList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantInfo), MerchantEntity.class);
        List<MerchantEntity> childList = merchantService.listAllChild(allList, merchant);
        childList.add(merchant);
        if (childList != null && !childList.isEmpty()) {
            childList.forEach(data -> {
                data.setOrderPermission(orderPermission);
            });
            merchantService.updateBatchById(childList);
            asyncRedisService.asyncReportQrcode();
        }
        return AjaxResult.success();
    }

    @ApiOperation("获取码商列表")
    @PreAuthorize("@ss.hasPermi('system:merchantChild:list')")
    @PostMapping("/list")
    public R<Page<MerchantEntity>> userList(@RequestBody MerchantQueryDTO queryDTO) {
        Page<MerchantEntity> rowPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        //queryWrapper组装查询where条件
        LoginUser securityUser = SecurityUtils.getLoginUser();
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantEntity::getParentId, securityUser.getUser().getUserId())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getUserId()), MerchantEntity::getUserId, queryDTO.getUserId())
                .like(StrUtil.isNotEmpty(queryDTO.getUserName()), MerchantEntity::getUserName, queryDTO.getUserName())
                .like(StrUtil.isNotEmpty(queryDTO.getParentName()), MerchantEntity::getParentName, queryDTO.getParentName())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getWorkStatus()), MerchantEntity::getWorkStatus, queryDTO.getWorkStatus())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getOrderPermission()), MerchantEntity::getOrderPermission, queryDTO.getOrderPermission())
                .orderByAsc(MerchantEntity::getWorkStatus,MerchantEntity::getOrderPermission)
                .orderByDesc(MerchantEntity::getBalance);

        Page<MerchantEntity> page = merchantService.page(rowPage, queryWrapper);
        if (page != null && page.getRecords() != null && !page.getRecords().isEmpty()) {
            //获取所有下级码商
            List<MerchantEntity> allList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantInfo), MerchantEntity.class);
            Map<Long, SysUser> userMap = userService.selectOnlyUserInfoById(page.getRecords().stream().map(MerchantEntity::getUserId).collect(Collectors.toList()));
            List<MerchantDepositVO> poList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantDeposit), MerchantDepositVO.class);
            Map<Long, BigDecimal> poMap = poList.stream().collect(Collectors.toMap(MerchantDepositVO::getUserId, MerchantDepositVO::getBaseDeposit,(key1,key2)->key2));
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
                //余额为当前码商余额
                if (redisUtils.hasKey(RedisKeys.merchantBalance + data.getUserId())) {
                    Object balance = redisUtils.get(RedisKeys.merchantBalance + data.getUserId());
                    if (ObjectUtil.isNotEmpty(balance)) {
                        data.setBalance(new BigDecimal(balance.toString()));
                    }
                }else {
                    data.setBalance(new BigDecimal(0));
                }
                //所有下级余额
                List<MerchantEntity> childList = merchantService.listAllChild(allList, data);
                BigDecimal childBalance = new BigDecimal(0);
                if (!childList.isEmpty()) {
                    for (MerchantEntity child : childList) {
                        if (redisUtils.hasKey(RedisKeys.merchantBalance + child.getUserId())) {
                            Object balance = redisUtils.get(RedisKeys.merchantBalance + child.getUserId());
                            if (ObjectUtil.isNotEmpty(balance)) {
                                childBalance = childBalance.add(new BigDecimal(balance.toString()));
                            }
                        }
                    }
                }
                data.setChildBalance(childBalance);
                data.setTotalBalance(childBalance.add(data.getBalance()));
            });
        }
        return R.ok(page);
    }

    @ApiOperation("获取所有下级码商列表")
//    @PreAuthorize("@ss.hasPermi('system:merchantChild:allChildList')")
    @PostMapping("/allChildList")
    public R<Page<MerchantEntity>> allChildList(@RequestBody MerchantQueryDTO queryDTO) {
        Page<MerchantEntity> rowPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        //queryWrapper组装查询where条件
        LoginUser securityUser = SecurityUtils.getLoginUser();
        MerchantEntity self = merchantService.getById(securityUser.getUser().getUserId());
        if (!StrUtil.isNotEmpty(queryDTO.getParentPath())) {
            queryDTO.setParentPath(self.getParentPath());
        }
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(MerchantEntity::getParentPath, queryDTO.getParentPath())
                .ne(MerchantEntity::getUserId, self.getUserId())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getUserId()), MerchantEntity::getUserId, queryDTO.getUserId())
                .like(StrUtil.isNotEmpty(queryDTO.getUserName()), MerchantEntity::getUserName, queryDTO.getUserName())
                .like(StrUtil.isNotEmpty(queryDTO.getParentName()), MerchantEntity::getParentName, queryDTO.getParentName())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getWorkStatus()), MerchantEntity::getWorkStatus, queryDTO.getWorkStatus())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getOrderPermission()), MerchantEntity::getOrderPermission, queryDTO.getOrderPermission())
                .orderByAsc(MerchantEntity::getWorkStatus,MerchantEntity::getOrderPermission)
                .orderByDesc(MerchantEntity::getBalance);
        Page<MerchantEntity> page = merchantService.page(rowPage, queryWrapper);
        if (page != null && page.getRecords() != null && !page.getRecords().isEmpty()) {
            //获取所有下级码商
            List<MerchantEntity> allList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantInfo), MerchantEntity.class);
            Map<Long, SysUser> userMap = userService.selectOnlyUserInfoById(page.getRecords().stream().map(MerchantEntity::getUserId).collect(Collectors.toList()));
            List<MerchantDepositVO> poList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantDeposit), MerchantDepositVO.class);
            Map<Long, BigDecimal> poMap = poList.stream().collect(Collectors.toMap(MerchantDepositVO::getUserId, MerchantDepositVO::getBaseDeposit,(key1,key2)->key2));
            page.getRecords().forEach(data -> {
                if (data.getParentId().equals(self.getUserId())) {
                    data.setShowTrimBalance(0);
                } else {
                    data.setShowTrimBalance(1);
                }
                SysUser sysUser = userMap.get(data.getUserId());
                if (sysUser != null) {
                    data.setAllowLoginIp(sysUser.getAllowLoginIp());
                    data.setGoogleSecret(sysUser.getGoogleSecret());
                    data.setGoogleSecretFlag(sysUser.getGoogleSecretFlag());
                    data.setLastLoginIp(sysUser.getLoginIp());
                    data.setLastLoginTime(sysUser.getLoginDate());
                }
                data.setBaseDeposit(poMap.get(data.getUserId()));
                //余额为当前码商余额
                if (redisUtils.hasKey(RedisKeys.merchantBalance + data.getUserId())) {
                    Object balance = redisUtils.get(RedisKeys.merchantBalance + data.getUserId());
                    if (ObjectUtil.isNotEmpty(balance)) {
                        data.setBalance(new BigDecimal(balance.toString()));
                    }
                }else {
                    data.setBalance(new BigDecimal(0));
                }
                //所有下级余额
                List<MerchantEntity> childList = merchantService.listAllChild(allList, data);
                BigDecimal childBalance = new BigDecimal(0);
                if (!childList.isEmpty()) {
                    for (MerchantEntity child : childList) {
                        if (redisUtils.hasKey(RedisKeys.merchantBalance + child.getUserId())) {
                            Object balance = redisUtils.get(RedisKeys.merchantBalance + child.getUserId());
                            if (ObjectUtil.isNotEmpty(balance)) {
                                childBalance = childBalance.add(new BigDecimal(balance.toString()));
                            }
                        }
                    }
                }
                data.setChildBalance(childBalance);
                data.setTotalBalance(data.getBalance());
            });
        }
        return R.ok(page);
    }

    @ApiOperation("获取所有下级码商的码统计列表")
    @PostMapping("/allChildQrcodeTotal")
    public R<Page<MerchantQrcodeEntity>> allChildQrcodeTotal(MerchantQrcodeQueryDTO queryDTO){
        return R.ok(merchantQrcodeService.totalChildQrcode(queryDTO));
    }

    @ApiOperation("码商通道列表")
    @PreAuthorize("@ss.hasPermi('system:merchantChild:channelList')")
    @ApiParam(name = "shopId", value = "商户ID", required = true)
    @GetMapping("channelList")
    public R<List<MerchantChannelEntity>> channelList(@RequestParam("merchantId")Long merchantId) {
        MerchantEntity childMerchant = merchantService.getById(merchantId);
        MerchantEntity parentMerchant = merchantService.getById(SecurityUtils.getUserId());
        if (!childMerchant.getParentPath().startsWith(parentMerchant.getParentPath())) {
            return R.fail("非法访问");
        }
        List<MerchantChannelEntity> list = merchantChannelService.listByMerchantId(merchantId, null);
        return R.ok(list);
    }

    @Log(title = "修改码商通道列表", businessType = BusinessType.UPDATE)
    @ApiOperation("修改码商通道列表")
    @PreAuthorize("@ss.hasPermi('system:merchantChild:channelUpdate')")
    @PostMapping("channelUpdate")
    public AjaxResult channelUpdate(@RequestBody ChildMerchantChannelDTO childMerchantChannelDTO) {
        MerchantEntity childMerchant = merchantService.getById(childMerchantChannelDTO.getMerchantId());
        MerchantEntity parentMerchant = merchantService.getById(SecurityUtils.getUserId());
        if (!childMerchant.getParentPath().startsWith(parentMerchant.getParentPath()) && !SecurityUtils.hasRole(SysRoleEnum.AGENT.getRoleKey())) {
            return AjaxResult.error("非法访问");
        }
        merchantChannelService.updateMerchantChannel(BeanUtil.copyProperties(childMerchantChannelDTO, MerchantChannelEntity.class));
        return AjaxResult.success();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "childId", value = "下级代理ID"),
            @ApiImplicitParam(name = "amount", value = "转移金额"),
            @ApiImplicitParam(name = "code", value = "验证码"),
            @ApiImplicitParam(name = "remark", value = "备注")
    })
    @Log(title = "码商给下级码商转移金额", businessType = BusinessType.UPDATE)
    @ApiOperation("码商给下级码商转移金额")
    @PreAuthorize("@ss.hasPermi('system:merchantChild:trimBalance')")
    @GetMapping("trimBalance")
    public AjaxResult trimBalance(@RequestParam("childId")Long childId,
                                  @RequestParam("amount") BigDecimal amount,
                                  @RequestParam("code")String code,
                                  @RequestParam(value = "remark", required = false)String remark) {
        try {
            Integer googleNumber = Integer.parseInt(code);
        }catch (NumberFormatException e) {
            return AjaxResult.error("验证码错误");
        }
        SysUser sysUser = userService.selectUserById(SecurityUtils.getUserId());
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
        googleAuthenticator.setWindowSize(5);
        if (!googleAuthenticator.check_code(sysUser.getGoogleSecret(), Integer.parseInt(code), System.currentTimeMillis())) {
            return AjaxResult.error("验证码不正确");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return AjaxResult.error("不能转移负金额");
        }
        return childService.trimBalance(childId, amount, remark);
    }

    @Log(title = "删除码商", businessType = BusinessType.UPDATE)
    @ApiOperation("删除码商")
    @PreAuthorize("@ss.hasPermi('system:merchantChild:delete')")
    @GetMapping("delete")
    public AjaxResult delete(Long userId) {
        MerchantEntity merchantEntity = merchantService.getById(userId);
        if (merchantEntity == null || !merchantEntity.getParentId().equals(SecurityUtils.getUserId())) {
            return AjaxResult.error("您暂无权限进行此操作");
        }
        return merchantService.deleteMerchant(userId);
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "childId", value = "下级代理ID"),
            @ApiImplicitParam(name = "amount", value = "转移金额"),
            @ApiImplicitParam(name = "code", value = "验证码"),
            @ApiImplicitParam(name = "remark", value = "备注")
    })
    @Log(title = "码商自由转移金额", businessType = BusinessType.UPDATE)
    @ApiOperation("码商自由转移金额")
    @PreAuthorize("@ss.hasPermi('system:merchantChild:trimBalance')")
    @GetMapping("trimBalanceFree")
    public AjaxResult trimBalanceFree(@RequestParam("toId")Long toId,
                                  @RequestParam("amount") BigDecimal amount,
                                  @RequestParam("code")String code,
                                  @RequestParam(value = "remark", required = false)String remark) {
        if (!SecurityUtils.validGoogle(code)) {
            return AjaxResult.error("验证码不正确");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return AjaxResult.error("不能转移负金额");
        }
        return childService.trimBalanceFree(toId, amount, remark);
    }
}
