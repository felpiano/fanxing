
package com.ruoyi.system.service.business.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.enums.ChangeTypeEnum;
import com.ruoyi.common.enums.SysRoleEnum;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.*;
import com.ruoyi.system.domain.business.*;
import com.ruoyi.system.domain.dto.AmountChangeDTO;
import com.ruoyi.system.mapper.business.ShopMapper;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.business.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * <p>
 * 商户 服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-10-18
 */

@Service
@Slf4j
public class ShopServiceImp extends ServiceImpl<ShopMapper, ShopEntity> implements ShopService {
    @Resource
    private ISysUserService userService;
    @Resource
    private ChannelService channelService;
    @Resource
    private ShopBaseChannelService sbcService;
    @Resource
    private ShopMerchantRelationService shopMerchantRelationService;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private ShopAmountRecordsService shopAmountRecordsService;
    @Resource
    private ISysRoleService roleService;
    @Resource
    private AsyncRedisService asyncRedisService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult saveAgentShop(ShopEntity shopEntity) {
        SysUser newUser = new SysUser();
        newUser.setUserName(shopEntity.getUserName());
        if (!userService.checkUserNameUnique(newUser)) {
            return AjaxResult.error("新增商户'" + shopEntity.getUserName() + "'失败，登录账号已存在");
        }
        SysUser loginUser = SecurityUtils.getLoginUser().getUser();
        newUser.setUid(loginUser.getUid());
        newUser.setNickName(shopEntity.getShopName());
        newUser.setCreateBy(loginUser.getUserName());
        newUser.setPassword(SecurityUtils.encryptPassword(StringUtils.isNotEmpty(shopEntity.getLoginPassword())? shopEntity.getLoginPassword():"123456"));

        //设置用户角色为商户
        SysRole sysRole = roleService.getRoleByRoleKey(SysRoleEnum.SHOP.getRoleKey());
        Long[] roleIds = {sysRole.getRoleId()};
        newUser.setRoleIds(roleIds);
        //设置用户其他属性
        newUser.setIdentity((int)SysRoleEnum.SHOP.getRoleId());
        newUser.setGoogleSecretFlag(0);
        newUser.setAllowLoginIp(shopEntity.getAllowLoginIp());
        //保存用户
        boolean flag = userService.insertUser(newUser) > 0;
        //设置商户ID
        shopEntity.setUserId(newUser.getUserId());
        try {
            shopEntity.setSignSecret(DESUtil.createSecret());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        //如果未设置所属代理，谁创建的属于谁的商户
        if (ObjectUtil.isNull(shopEntity.getAgentId())) {
            shopEntity.setAgentId(loginUser.getUserId());
            shopEntity.setAgentName(loginUser.getUserName());
        }
        flag = flag && baseMapper.insert(shopEntity) > 0;
        //保存商户基础通道
        List<ShopBaseChannelEntity> sbcList = new ArrayList<>();
        List<ChannelEntity> channelList = channelService.list();
        if (channelList != null && !channelList.isEmpty()) {
            channelList.forEach(data -> {
                ShopBaseChannelEntity sbc = ShopBaseChannelEntity.builder()
                        .shopId(shopEntity.getUserId())
                        .channelId(data.getId())
                        .channelRate(BigDecimal.valueOf(0))
                        .build();
                sbcList.add(sbc);
            });
            sbcService.saveBatch(sbcList);
        }

        //将余额保存至redis
        redisUtils.set(RedisKeys.shopBalance + shopEntity.getUserId(), ObjectUtil.isNotEmpty(shopEntity.getBalance())? shopEntity.getBalance().toString():"0");
        asyncRedisService.asyncReportQrcode();
        return flag? AjaxResult.success():AjaxResult.error();
    }

    @Override
    @Transactional
    public AjaxResult updateGoogleFlag(Long userId, Integer googleSecretFlag) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setGoogleSecretFlag(googleSecretFlag);
        userService.updateUser(sysUser);
        return AjaxResult.success();
    }

    @Override
    @Transactional
    public AjaxResult deleteAgentShop(Long userId) {
        ShopEntity shopData = baseMapper.selectById(userId);
        if (!SecurityUtils.getUserId().equals(shopData.getAgentId())) {
            return AjaxResult.error("您暂无权限删除此商户");
        }
        boolean flag = userService.deleteUserById(userId) > 0;
        flag = flag && this.removeById(userId);
        //删除商户基础通道
        sbcService.remove(Wrappers.lambdaQuery(ShopBaseChannelEntity.class).eq(ShopBaseChannelEntity::getShopId, userId));
        //删除商户与码商关联关系
        shopMerchantRelationService.remove(Wrappers.lambdaQuery(ShopMerchantRelationEntity.class).eq(ShopMerchantRelationEntity::getShopId, userId));
        asyncRedisService.asyncReportQrcode();
        return flag? AjaxResult.success():AjaxResult.error();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult updateShop(ShopEntity shop) {
        ShopEntity shopData = baseMapper.selectById(shop.getUserId());
        if (!SecurityUtils.getUserId().equals(shopData.getAgentId())) {
            return AjaxResult.error("您暂无权限修改此商户");
        }
        if (StrUtil.isNotEmpty(shop.getGoogleSecret()) || ObjectUtil.isNotEmpty(shop.getGoogleSecretFlag())
                || StrUtil.isNotEmpty(shop.getAllowLoginIp()) || ObjectUtil.isNotEmpty(shop.getLoginPassword())) {
            SysUser agentUser = new SysUser();
            agentUser.setUserId(shop.getUserId());
            if (StrUtil.isNotEmpty(shop.getGoogleSecret())) {
                agentUser.setGoogleSecret(shop.getGoogleSecret());
            }
            if (ObjectUtil.isNotEmpty(shop.getGoogleSecretFlag())) {
                agentUser.setGoogleSecretFlag(shop.getGoogleSecretFlag());
            }
            if (StrUtil.isNotEmpty(shop.getAllowLoginIp())) {
                agentUser.setAllowLoginIp(shop.getAllowLoginIp());
            }
            if (StrUtil.isNotEmpty(shop.getLoginPassword())) {
                agentUser.setPassword(SecurityUtils.encryptPassword(shop.getLoginPassword()));
            }
            userService.updateOnlyUser(agentUser);
        }

        if (StrUtil.isNotEmpty(shop.getSafeCode())) {
            shop.setSafeCode(SecurityUtils.encryptPassword(shop.getSafeCode()));
        }
        boolean flag = baseMapper.updateById(shop) > 0;
        //绑定码商
        shopMerchantRelationService.bindShopAndMerchant(shop.getUserId(), shop.getMerchantIds());
        asyncRedisService.asyncReportQrcode();
        return flag ? AjaxResult.success():AjaxResult.error();
    }

    @Override
    public ShopEntity getShopById(Long id) {
        ShopEntity shopEntity = baseMapper.selectById(id);
        if (ObjectUtil.isNotNull(shopEntity)) {
            SysUser sysUser = userService.selectUserById(id);
            shopEntity.setGoogleSecretFlag(sysUser.getGoogleSecretFlag());
            List<MerchantEntity> list = shopMerchantRelationService.getRelationByShopId(id);
            if (list != null && !list.isEmpty()) {
                shopEntity.setMerchantIds(list.stream().map(a -> String.valueOf(a.getUserId())).collect(Collectors.joining(",")));
            }
        }
        return shopEntity;
    }

    @Override
    public void updateShopAmount(AmountChangeDTO shopAmountDTO) {
        try {
            String moneyKyes = RedisKeys.shopBalance + shopAmountDTO.getUserId();
            //redis中获取商户余额
            if (!redisUtils.hasKey(moneyKyes)) {
                redisUtils.set(moneyKyes, "0");
            }
            boolean redisOpFlag = redisUtils.addMonery(moneyKyes, shopAmountDTO.getChangeAmount().toString());
            if (redisOpFlag) {
                try {
                    BigDecimal afterAmount = new BigDecimal(redisUtils.get(moneyKyes));
                    BigDecimal shopBalance = afterAmount.subtract(shopAmountDTO.getChangeAmount());
                    ShopAmountRecordsEntity recordsEntity = ShopAmountRecordsEntity.builder()
                            .userId(shopAmountDTO.getUserId())
                            .userName(shopAmountDTO.getUserName())
                            .changeType(ObjectUtil.isNotEmpty(shopAmountDTO.getChangeType())?shopAmountDTO.getChangeType():1)
                            .amountType(ObjectUtil.isNotEmpty(shopAmountDTO.getAmountType())? shopAmountDTO.getAmountType():1)
                            .beforeAmount(shopBalance)
                            .changeAmount(shopAmountDTO.getChangeAmount())
                            .afterAmount(afterAmount)
                            .createTime(new Date())
                            .notes(shopAmountDTO.getNotes())
                            .remarks(shopAmountDTO.getRemarks())
                            .orderNo(shopAmountDTO.getOrderNo())
                            .agentId(shopAmountDTO.getAgentId())
                            .build();
                    shopAmountRecordsService.save(recordsEntity);
                    //更新钱包
                    ShopEntity agentEntity = ShopEntity.builder()
                            .userId(shopAmountDTO.getUserId())
                            .balance(afterAmount)
                            .build();
                    baseMapper.updateById(agentEntity);
                }catch (Exception e) {
                    log.error("订单-{}增加商户余额，redis已完成增加，更新账变和钱包时异常：{}", shopAmountDTO.getOrderNo(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("商户变更余额失败-{},系统订单号：{}", e.getMessage(), shopAmountDTO.getOrderNo());
            throw new ServiceException("商户余额增加失败");
        }
    }

}

