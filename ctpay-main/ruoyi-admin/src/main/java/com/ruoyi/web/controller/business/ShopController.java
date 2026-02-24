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
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.enums.BusinessConfigKey;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.enums.SysRoleEnum;
import com.ruoyi.common.utils.DESUtil;
import com.ruoyi.common.utils.GoogleAuthenticator;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.business.AgentEntity;
import com.ruoyi.system.domain.business.ShopEntity;
import com.ruoyi.system.domain.dto.AmountChangeDTO;
import com.ruoyi.system.domain.dto.ShopQueryDTO;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.business.ShopService;
import com.ruoyi.system.service.business.impl.AsyncRedisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.aspectj.weaver.loadtime.Aj;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 商户 前端控制器
 * </p>
 *
 * @author admin  ALTER TABLE sys_user AUTO_INCREMENT = 10002;
 * @since 2024-09-13
 */
@RestController
@RequestMapping("/shopEntity")
@Api("商户管理")
public class ShopController extends BaseController {

    @Resource
    private ShopService shopService;
    @Resource
    private ISysUserService userService;
    @Resource
    private ISysConfigService configService;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private AsyncRedisService asyncRedisService;

    @ApiOperation("获取商户列表")
    @PreAuthorize("@ss.hasPermi('system:shop:list')")
    @PostMapping("/list")
    public R<Page<ShopEntity>> userList(@RequestBody ShopQueryDTO queryDTO) {
        Page<ShopEntity> rowPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        //queryWrapper组装查询where条件
        LoginUser securityUser = SecurityUtils.getLoginUser();
        LambdaQueryWrapper<ShopEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(!securityUser.getUser().isAdmin(), ShopEntity::getAgentId, securityUser.getUserId())
                .like(StrUtil.isNotEmpty(queryDTO.getAgentName()), ShopEntity::getAgentName, queryDTO.getAgentName())
                .like(StrUtil.isNotEmpty(queryDTO.getUserName()), ShopEntity::getUserName, queryDTO.getUserName())
                .like(StrUtil.isNotEmpty(queryDTO.getShopName()), ShopEntity::getShopName, queryDTO.getShopName())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getStatus()), ShopEntity::getStatus, queryDTO.getStatus())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getUserId()), ShopEntity::getUserId, queryDTO.getUserId())
                .ge(ObjectUtil.isNotEmpty(queryDTO.getMinAmount()), ShopEntity::getMinAmount, queryDTO.getMinAmount())
                .le(ObjectUtil.isNotEmpty(queryDTO.getMaxAmount()), ShopEntity::getMaxAmount, queryDTO.getMaxAmount())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getDelFlag()), ShopEntity::getDelFlag, queryDTO.getDelFlag());
        Page<ShopEntity> page = shopService.page(rowPage, queryWrapper);
        if (page != null && page.getRecords() != null && !page.getRecords().isEmpty()) {
            Map<Long, SysUser> userMap = userService.selectOnlyUserInfoById(page.getRecords().stream().map(ShopEntity::getUserId).collect(Collectors.toList()));
            page.getRecords().forEach(shop -> {
                SysUser sysUser = userMap.get(shop.getUserId());
                if (sysUser != null) {
                    shop.setAllowLoginIp(sysUser.getAllowLoginIp());
                    shop.setGoogleSecret(sysUser.getGoogleSecret());
                    shop.setGoogleSecretFlag(sysUser.getGoogleSecretFlag());
                    shop.setLastLoginIp(sysUser.getLoginIp());
                    shop.setLastLoginTime(sysUser.getLoginDate());
                    shop.setClientIp(configService.selectConfigByKey(BusinessConfigKey.CLIENTIP.getConfigKey()));
                    shop.setSubmitOrderUrl(configService.selectConfigByKey(BusinessConfigKey.SUBMITORDERURL.getConfigKey()));
                }
                if (redisUtils.hasKey(RedisKeys.shopBalance + shop.getUserId())) {
                    Object balance = redisUtils.get(RedisKeys.shopBalance + shop.getUserId());
                    if (ObjectUtil.isNotEmpty(balance)) {
                        shop.setBalance(new BigDecimal(balance.toString()));
                    }
                }else {
                    shop.setBalance(new BigDecimal(0));
                }
            });
        }
        return R.ok(page);
    }

    @ApiOperation("获取商户列表-不分页")
    @PreAuthorize("@ss.hasPermi('system:shop:listNoPage')")
    @PostMapping("/listNoPage")
    public R<List<ShopEntity>> listNoPage(@RequestBody ShopQueryDTO queryDTO) {
        LoginUser securityUser = SecurityUtils.getLoginUser();
        LambdaQueryWrapper<ShopEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(!securityUser.getUser().isAdmin(), ShopEntity::getAgentId, getUserId())
                .like(StrUtil.isNotEmpty(queryDTO.getUserName()), ShopEntity::getUserName, queryDTO.getUserName())
                .like(StrUtil.isNotEmpty(queryDTO.getShopName()), ShopEntity::getShopName, queryDTO.getShopName())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getStatus()), ShopEntity::getStatus, queryDTO.getStatus())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getUserId()), ShopEntity::getUserId, queryDTO.getUserId())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getDelFlag()), ShopEntity::getDelFlag, queryDTO.getDelFlag())
                .select(ShopEntity::getUserId, ShopEntity::getUserName, ShopEntity::getShopName, ShopEntity::getStatus, ShopEntity::getMinAmount, ShopEntity::getMaxAmount);
        return R.ok(shopService.list(queryWrapper));
    }

    @ApiOperation("根据id获取商户")
    @PreAuthorize("@ss.hasPermi('system:shop:list')")
    @GetMapping("/getById")
    public R<ShopEntity> getById(@RequestParam("userId") Long userId) {
        return R.ok(shopService.getShopById(userId));
    }

    @ApiOperation("查看密钥")
    @PreAuthorize("@ss.hasPermi('system:shop:showSecret')")
    @GetMapping("/showSecret")
    public R<String> showSecret(@RequestParam("userId") Long userId, @RequestParam(value = "safeCode", required = false) String safeCode) {
        ShopEntity shopEntity = shopService.getById(userId);
        if (StrUtil.isEmpty(shopEntity.getSafeCode()) || SecurityUtils.matchesPassword(safeCode, shopEntity.getSafeCode())) {
            return R.ok(shopEntity.getSignSecret());
        } else {
            return R.fail("安全码不正确");
        }
    }

    @ApiOperation("修改密钥")
    @PreAuthorize("@ss.hasPermi('system:shop:updateSecret')")
    @GetMapping("/resetScret")
    public AjaxResult resetScret(@RequestParam("userId") Long userId) {
        try {
            String secret = DESUtil.createSecret();
            ShopEntity shopEntity = ShopEntity.builder().userId(userId).signSecret(secret).build();
            shopService.updateById(shopEntity);
            return AjaxResult.success("成功", secret);
        } catch (NoSuchAlgorithmException e) {
            return AjaxResult.error("重置密钥失败");
        }
    }

    @Log(title = "新增商户", businessType = BusinessType.INSERT)
    @ApiOperation("新增商户")
    @PreAuthorize("@ss.hasPermi('system:shop:add')")
    @PostMapping("add")
    public AjaxResult add(@Validated @RequestBody ShopEntity shop) {
        if (SecurityUtils.getLoginUser().getUser().getIdentity() != 3) {
            return AjaxResult.error("您暂时无权限新增商户");
        }
        return shopService.saveAgentShop(shop);
    }

    @Log(title = "修改商户", businessType = BusinessType.INSERT)
    @ApiOperation("修改商户")
    @PreAuthorize("@ss.hasPermi('system:shop:update')")
    @PostMapping("update")
    public AjaxResult update(@Validated @RequestBody ShopEntity shopEntity) {
        return shopService.updateShop(shopEntity);
    }

    @Log(title = "删除商户", businessType = BusinessType.DELETE)
    @ApiOperation("删除商户")
    @PreAuthorize("@ss.hasPermi('system:shop:delete')")
    @GetMapping("deleteAgentShop")
    public AjaxResult deleteAgentShop(@RequestParam("userId")Long userId) {
        return shopService.deleteAgentShop(userId);
    }

    @Log(title = "修改商户状态", businessType = BusinessType.UPDATE)
    @ApiOperation("修改商户状态")
    @PreAuthorize("@ss.hasPermi('system:shop:updateStatus')")
    @GetMapping("upateStatus")
    public AjaxResult upateStatus(@RequestParam("userId")Long userId, @RequestParam("status")Integer status) {
        ShopEntity shopData = shopService.getById(userId);
        if (!SecurityUtils.getUserId().equals(shopData.getAgentId())) {
            return AjaxResult.error("您暂无权修改此商户");
        }
        ShopEntity shopEntity = ShopEntity.builder().status(status).userId(userId).build();
        boolean flag = shopService.updateById(shopEntity);
        asyncRedisService.asyncReportQrcode();
        return flag?AjaxResult.success():AjaxResult.error();
    }

    @Log(title = "修改商户会员名称输入框", businessType = BusinessType.UPDATE)
    @ApiOperation("修改商户会员名称输入框")
    @PreAuthorize("@ss.hasPermi('system:shop:updateStatus')")
    @GetMapping("updateMemberFlag")
    public AjaxResult updateMemberFlag(@RequestParam("userId")Long userId, @RequestParam("memberFlag")Integer memberFlag) {
        ShopEntity shopData = shopService.getById(userId);
        if (!SecurityUtils.getUserId().equals(shopData.getAgentId())) {
            return AjaxResult.error("您暂无权修改此商户");
        }
        ShopEntity shopEntity = ShopEntity.builder().memberNameFlag(memberFlag).userId(userId).build();
        boolean flag = shopService.updateById(shopEntity);
        return flag?AjaxResult.success():AjaxResult.error();
    }

    @Log(title = "修改商户进单状态", businessType = BusinessType.UPDATE)
    @ApiOperation("修改商户进单状态")
    @PreAuthorize("@ss.hasPermi('system:shop:enterOrder')")
    @GetMapping("upateEnterOrder")
    public AjaxResult upateEnterOrder(@RequestParam("enterOrder")Integer enterOrder) {
        List<ShopEntity> list = shopService.list(Wrappers.lambdaQuery(ShopEntity.class).eq(ShopEntity::getAgentId, SecurityUtils.getUserId()));
        if (list != null && !list.isEmpty()) {
            list.forEach(data -> {
                data.setEnterOrder(enterOrder);
            });
            shopService.updateBatchById(list);
            asyncRedisService.asyncReportQrcode();
            return AjaxResult.success();
        }
        return AjaxResult.error();
    }

    @Log(title = "修改商户谷歌验证码状态", businessType = BusinessType.UPDATE)
    @ApiOperation("修改商户谷歌验证码状态")
    @PreAuthorize("@ss.hasPermi('system:shop:updateGoogle')")
    @GetMapping("updateGoogleFlag")
    public AjaxResult updateGoogleFlag(@RequestParam("userId")Long userId, @RequestParam("googleSecretFlag")Integer googleSecretFlag) {
        ShopEntity shopData = shopService.getById(userId);
        if (!SecurityUtils.getUserId().equals(shopData.getAgentId())) {
            return AjaxResult.error("您暂无权修改此商户");
        }
        return shopService.updateGoogleFlag(userId, googleSecretFlag);
    }

    @Log(title = "解绑商户tg群组", businessType = BusinessType.UPDATE)
    @ApiOperation("解绑商户tg群组")
    @PreAuthorize("@ss.hasPermi('system:shop:bindTg')")
    @GetMapping("unBindTelegramGroup")
    public AjaxResult unBindTelegramGroup(@RequestParam("userId")Long userId) {
        ShopEntity shopEntity = ShopEntity.builder().telegramGroup("").userId(userId).build();
        return shopService.updateById(shopEntity)?AjaxResult.success():AjaxResult.error();
    }

    @Log(title = "绑定tg群组", businessType = BusinessType.UPDATE)
    @ApiOperation("绑定tg群组")
    @PreAuthorize("@ss.hasPermi('system:shop:bindTg')")
    @GetMapping("bingTg")
    public AjaxResult bingTg(@RequestParam("userId")Long userId, @RequestParam("telegramGroup")String telegramGroup) {
        ShopEntity shopEntity = ShopEntity.builder().telegramGroup(telegramGroup).userId(userId).build();
        return shopService.updateById(shopEntity)?AjaxResult.success():AjaxResult.error();
    }

    @Log(title = "一键解绑商户tg群组", businessType = BusinessType.UPDATE)
    @ApiOperation("一键解绑商户tg群组")
    @PreAuthorize("@ss.hasPermi('system:shop:bindTg')")
    @GetMapping("unBindTelegramGroupOneKey")
    public AjaxResult unBindTelegramGroupOneKey() {
        List<ShopEntity> list = shopService.list(
                Wrappers.lambdaQuery(ShopEntity.class).eq(ShopEntity::getAgentId, SecurityUtils.getUserId()));
        if (list != null && !list.isEmpty()) {
            list.forEach(data -> {
                data.setTelegramGroup("");
            });
            shopService.updateBatchById(list);
        }
        return AjaxResult.success();
    }

    @Log(title = "充值余额", businessType = BusinessType.UPDATE)
    @ApiOperation("充值余额")
    @PreAuthorize("@ss.hasPermi('system:shop:chargeAmount')")
    @GetMapping("chargeAmount")
    public AjaxResult chargeAmount(@RequestParam("userId")Long userId, @RequestParam("amount") BigDecimal amount,@RequestParam(value = "remark",required = false)String remark, @RequestParam("code")String code) {
        if (!SecurityUtils.hasRole(SysRoleEnum.AGENT.getRoleKey())) {
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
        ShopEntity shopEntity = shopService.getById(userId);
        if (!SecurityUtils.getUserId().equals(shopEntity.getAgentId())) {
            return AjaxResult.error("您暂无权限操作此商户");
        }
        shopService.updateShopAmount(AmountChangeDTO.builder()
                .userId(userId)
                .userName(shopEntity.getUserName())
                .changeAmount(amount)
                .agentId(shopEntity.getAgentId())
                .amountType(1)
                .changeType(2)
                .remarks(remark).build());
        return AjaxResult.success();
    }
}
