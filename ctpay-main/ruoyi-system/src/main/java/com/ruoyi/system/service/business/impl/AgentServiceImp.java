package com.ruoyi.system.service.business.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.enums.ChangeTypeEnum;
import com.ruoyi.common.enums.SysRoleEnum;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.GoogleAuthenticator;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.business.*;
import com.ruoyi.system.domain.dto.AmountChangeDTO;
import com.ruoyi.system.mapper.business.AgentAmountRecordsMapper;
import com.ruoyi.system.mapper.business.AgentMapper;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.business.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 代理基础信息表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-09-08
 */
@Service
@Slf4j
public class AgentServiceImp extends ServiceImpl<AgentMapper, AgentEntity> implements AgentService {

    @Resource
    private ISysUserService userService;
    @Resource
    private ISysRoleService roleService;
    @Resource
    private ISysDeptService deptService;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private AgentAmountRecordsService agentAmountRecordsService;
    @Resource
    private AgentChannelService agentChannelService;
    @Resource
    private BaseChannelService baseChannelService;
    @Resource
    private AsyncRedisService asyncRedisService;

    @Override
    @Transactional
    public AjaxResult saveAgent(SysUser user) {
        deptService.checkDeptDataScope(user.getDeptId());
        roleService.checkRoleDataScope(user.getRoleIds());
        if (!userService.checkUserNameUnique(user)) {
            return AjaxResult.error("新增代理'" + user.getUserName() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return AjaxResult.error("新增代理'" + user.getUserName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return AjaxResult.error("新增代理'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        SysUser loginUser = SecurityUtils.getLoginUser().getUser();
        user.setCreateBy(loginUser.getUserName());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        //设置用户角色为代理
        SysRole sysRole = roleService.getRoleByRoleKey(SysRoleEnum.AGENT.getRoleKey());
        Long[] roleIds = {sysRole.getRoleId()};
        user.setRoleIds(roleIds);
        //设置用户其他属性
        user.setIdentity((int)SysRoleEnum.AGENT.getRoleId());
        user.setGoogleSecretFlag(0);
        user.setUid(StrUtil.uuid().substring(0, 8));
        //保存用户
        Boolean flag = userService.insertUser(user) > 0 ? true:false;
        //代理
        AgentEntity agentEntity = new AgentEntity();
        agentEntity.setNickName(user.getNickName());
        agentEntity.setUserName(user.getUserName());
        agentEntity.setUserId(user.getUserId());
        flag = flag && save(agentEntity);
        //同步增加代理通道产品
        List<BaseChannelEntity> baseChannelList = baseChannelService.list();
        if (baseChannelList != null && !baseChannelList.isEmpty()) {
            List<AgentChannelEntity> agentCList = BeanUtil.copyToList(baseChannelList, AgentChannelEntity.class);
            agentCList.forEach(data -> {
                data.setId(null);
                data.setAgentId(user.getUserId());
            });
            agentChannelService.saveBatch(agentCList);
        }
        asyncRedisService.asyncReportQrcode();
        //将余额保存至redis
        redisUtils.set(RedisKeys.agentBalance + agentEntity.getUserId(), ObjectUtil.isNotEmpty(agentEntity.getBalance())? agentEntity.getBalance().toString():"0", -1);
        return flag?AjaxResult.success():AjaxResult.error();
    }

    @Override
    @Transactional
    public AjaxResult updateAgent(AgentEntity agentEntity) {
        if (StrUtil.isNotEmpty(agentEntity.getGoogleSecret()) || ObjectUtil.isNotEmpty(agentEntity.getGoogleSecretFlag())
        || StrUtil.isNotEmpty(agentEntity.getAllowLoginIp()) || StrUtil.isNotEmpty(agentEntity.getPassword())) {
            SysUser agentUser = new SysUser();
            agentUser.setUserId(agentEntity.getUserId());
            if (StrUtil.isNotEmpty(agentEntity.getGoogleSecret())) {
                agentUser.setGoogleSecret(agentEntity.getGoogleSecret());
            }
            if (ObjectUtil.isNotEmpty(agentEntity.getGoogleSecretFlag())) {
                agentUser.setGoogleSecretFlag(agentEntity.getGoogleSecretFlag());
            }
            if (StrUtil.isNotEmpty(agentEntity.getAllowLoginIp())) {
                agentUser.setAllowLoginIp(agentEntity.getAllowLoginIp());
            }
            if (StrUtil.isNotEmpty(agentEntity.getPassword())) {
                agentUser.setPassword(SecurityUtils.encryptPassword(agentEntity.getPassword()));
            }
            userService.updateOnlyUser(agentUser);
        }
        baseMapper.updateById(agentEntity);
        asyncRedisService.asyncReportQrcode();
        return AjaxResult.success();
    }

    @Override
    @Transactional
    public AjaxResult deleteAgent(Long userId) {
        userService.deleteUserById(userId);
        //删除代理通道
        agentChannelService.remove(Wrappers.lambdaQuery(AgentChannelEntity.class).eq(AgentChannelEntity::getAgentId, userId));
        this.removeById(userId);
        asyncRedisService.asyncReportQrcode();
        return AjaxResult.success();
    }

    @Override
    public AjaxResult updateAgentBalance(AmountChangeDTO dto) {
        try {
            String moneyKyes = RedisKeys.agentBalance + dto.getUserId();
            //redis中获取商户余额
            if (!redisUtils.hasKey(moneyKyes)) {
                redisUtils.set(moneyKyes, "0");
            }
            boolean redisOpFlag = redisUtils.addMonery(moneyKyes, dto.getChangeAmount().toString());
            if (redisOpFlag) {
                try {
                    BigDecimal afterAmount = new BigDecimal(redisUtils.get(moneyKyes));
                    BigDecimal shopBalance = afterAmount.subtract(dto.getChangeAmount());
                    AgentAmountRecordsEntity recordsEntity = AgentAmountRecordsEntity.builder()
                            .userId(dto.getUserId())
                            .userName(dto.getUserName())
                            .changeType(ObjectUtil.isNotEmpty(dto.getChangeType())? dto.getChangeType():1)
                            .amountType(ObjectUtil.isNotEmpty(dto.getAmountType())? dto.getAmountType():1)
                            .beforeAmount(shopBalance)
                            .changeAmount(dto.getChangeAmount())
                            .afterAmount(afterAmount)
                            .createTime(new Date())
                            .notes(dto.getNotes())
                            .remarks(dto.getRemarks())
                            .orderNo(dto.getOrderNo())
                            .build();
                    agentAmountRecordsService.save(recordsEntity);
                    //更新钱包
                    AgentEntity agentEntity = AgentEntity.builder()
                            .userId(dto.getUserId())
                            .balance(afterAmount)
                            .build();
                    baseMapper.updateById(agentEntity);
                    asyncRedisService.asyncReportQrcode();
                }catch (Exception e) {
                    log.error("订单-{}代理变更余额，redis已完成增加，更新账变和钱包时异常：{}", dto.getOrderNo(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("代理变更余额失败-{},系统订单号：{}", e.getMessage(), dto.getOrderNo());
            throw new ServiceException("代理余额增加失败");
        }
        return AjaxResult.success();
    }
}
