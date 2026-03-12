package com.ruoyi.system.service.business.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.enums.ChangeTypeEnum;
import com.ruoyi.common.enums.SysRoleEnum;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.*;
import com.ruoyi.system.domain.business.*;
import com.ruoyi.system.domain.dto.AmountChangeDTO;
import com.ruoyi.system.domain.vo.MerchantDepositVO;
import com.ruoyi.system.domain.vo.ParentMerchantVO;
import com.ruoyi.system.mapper.business.MerchantChannelMapper;
import com.ruoyi.system.mapper.business.MerchantMapper;
import com.ruoyi.system.mapper.business.MerchantQrcodeMapper;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.business.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 码商 服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
@Slf4j(topic = "ct-business")
@Service
public class MerchantServiceImp extends ServiceImpl<MerchantMapper, MerchantEntity> implements MerchantService {

    @Resource
    private ISysUserService userService;

    @Resource
    private ChannelService channelService;

    @Resource
    private MerchantChannelService merchantChannelService;

    @Resource
    private MerchantAmountRecordsService merchantAmountRecordsService;

    @Resource
    private ISysRoleService roleService;

    @Resource
    private RedisUtils redisUtils;
    @Resource
    private RedisLock redisLock;
    @Resource
    private ShopMerchantRelationService shopMerchantRelationService;
    @Resource
    private AsyncRedisService asyncRedisService;
    @Autowired
    private MerchantQrcodeMapper merchantQrcodeMapper;
    @Resource
    private RedisCache redisCache;
    @Autowired
    private MerchantChannelMapper merchantChannelMapper;

    @Override
    public AjaxResult addMerchant(MerchantEntity merchant) {
        SysUser newUser = new SysUser();
        newUser.setUserName(merchant.getUserName());
        if (!userService.checkUserNameUnique(newUser)) {
            return AjaxResult.error("新增码商'" + merchant.getUserName() + "'失败，登录账号已存在");
        }
        SysUser loginUser = SecurityUtils.getLoginUser().getUser();
        newUser.setNickName(merchant.getMerchantName());
        newUser.setCreateBy(loginUser.getUserName());
        newUser.setPassword(SecurityUtils.encryptPassword(StringUtils.isNotEmpty(merchant.getLoginPassword())? merchant.getLoginPassword():"123456"));
        //设置用户角色为码商
        SysRole sysRole = roleService.getRoleByRoleKey(SysRoleEnum.MERCHANT.getRoleKey());
        Long[] roleIds = {sysRole.getRoleId()};
        newUser.setRoleIds(roleIds);
        //设置用户其他属性
        newUser.setIdentity((int)SysRoleEnum.MERCHANT.getRoleId());
        newUser.setGoogleSecretFlag(0);
        newUser.setUid(loginUser.getUid());
        merchant.setMerchantLevel(1);
        merchant.setParentName("");
        merchant.setParentId(null);
        merchant.setParentPath("");
        //如果设置的上级码商
        MerchantEntity parentMerchant = null;
        if (ObjectUtil.isNotEmpty(merchant.getParentId())) {
            parentMerchant = this.getById(merchant.getParentId());
            if (ObjectUtil.isNotNull(parentMerchant)) {
                if (StrUtil.isNotEmpty(parentMerchant.getParentPath()) && parentMerchant.getParentPath().split("/").length == 5) {
                    return AjaxResult.error("上级层级已超出限制");
                }
            }
        }
        //保存用户
        boolean flag = userService.insertUser(newUser) > 0;
        //设置商户ID
        merchant.setUserId(newUser.getUserId());
        merchant.setLoginPassword(newUser.getPassword());
        if (parentMerchant != null) {
            merchant.setParentName(parentMerchant.getMerchantName());
            merchant.setParentPath(StrUtil.isEmpty(parentMerchant.getParentPath())
                    ? parentMerchant.getUserId() + "/" :
                    parentMerchant.getParentPath() + merchant.getUserId() + "/");
            String[] levels = merchant.getParentPath().split("/");
            merchant.setMerchantLevel(levels.length);
        }

        //如果未设置所属代理，谁创建的属于谁的商户
        if (ObjectUtil.isNull(merchant.getAgentId())) {
            merchant.setAgentId(loginUser.getUserId());
        }
        if (ObjectUtil.isEmpty(merchant.getParentId())) {
            merchant.setParentPath(newUser.getUserId() + "/");
        }
        flag = flag && baseMapper.insert(merchant) > 0;
        //保存码商基础通道
        List<MerchantChannelEntity> merChannelList = new ArrayList<>();
        List<ChannelEntity> channelList = channelService.list();
        if (channelList != null && !channelList.isEmpty()) {
            channelList.forEach(data -> {
                MerchantChannelEntity sbc = MerchantChannelEntity.builder()
                        .merchantId(merchant.getUserId())
                        .channelId(data.getId())
                        .channelRate(BigDecimal.valueOf(0))
                        .minAmount(BigDecimal.valueOf(0))
                        .maxAmount(BigDecimal.valueOf(0))
                        .status(0)
                        .build();
                merChannelList.add(sbc);
            });
            merchantChannelService.saveBatch(merChannelList);
        }

        //将余额和冻结金额保存至redis
        redisUtils.set(RedisKeys.merchantBalance + merchant.getUserId(), ObjectUtil.isNotEmpty(merchant.getBalance())? merchant.getBalance().toString():"0");
        redisUtils.set(RedisKeys.merchantPrepayment + merchant.getUserId(), ObjectUtil.isNotEmpty(merchant.getPrepayment())? merchant.getPrepayment().toString():"0");
        redisUtils.set(RedisKeys.merchantCommission + merchant.getUserId(), ObjectUtil.isNotEmpty(merchant.getFreezeAmount())? merchant.getFreezeAmount().toString():"0");
        asyncRedisService.asyncReportQrcode();
        return flag? AjaxResult.success():AjaxResult.error();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult updateMerchant(MerchantEntity merchant) {
        SysUser sysUser = userService.selectUserById(merchant.getUserId());
        if (StrUtil.isNotEmpty(merchant.getLoginPassword()) || ObjectUtil.isNotEmpty(merchant.getStatus())) {
            if (StrUtil.isNotEmpty(merchant.getLoginPassword())) {
                sysUser.setPassword(SecurityUtils.encryptPassword(merchant.getLoginPassword()));
                merchant.setLoginPassword(sysUser.getPassword());
            }
            if (ObjectUtil.isNotEmpty(merchant.getStatus())) {
                sysUser.setStatus(merchant.getStatus().toString());
            }
            try {
                if (ObjectUtil.equals(1, merchant.getStatus())) {
                    Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
                    for (String key : keys) {
                        LoginUser user = JSONUtil.toBean(redisCache.getCacheObject(key).toString(), LoginUser.class);
                        if (user.getUserId().equals(merchant.getUserId())) {
                            redisCache.deleteObject(key);
                            break;
                        }
                    }
                }
            }catch (Exception e){
                log.error("强制退出登录码商失败");
            }
            userService.updateOnlyUser(sysUser);
        }
        if (StrUtil.isNotEmpty(merchant.getSafeCode())) {
            merchant.setSafeCode(SecurityUtils.encryptPassword(merchant.getSafeCode()));
        }
        //如果设置的上级码商
        if (ObjectUtil.isNotEmpty(merchant.getParentId())) {
            MerchantEntity parentMerchant = this.getById(merchant.getParentId());
            if (ObjectUtil.isNotNull(parentMerchant)) {
                merchant.setParentName(parentMerchant.getMerchantName());
                merchant.setParentPath(StrUtil.isEmpty(parentMerchant.getParentPath())
                        ?   parentMerchant.getUserId() + "/" :
                        parentMerchant.getParentPath()  +merchant.getUserId() + "/");
                String[] levels = merchant.getParentPath().split("/");
                merchant.setMerchantLevel(levels.length);
            }
        }

        boolean flag = baseMapper.updateById(merchant) > 0;
        asyncRedisService.asyncReportQrcode();
        return flag ? AjaxResult.success():AjaxResult.error();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult deleteMerchant(Long merchantId) {
        //是否有下级码商
        List<MerchantEntity> list = this.list(Wrappers.lambdaQuery(MerchantEntity.class)
                .eq(MerchantEntity::getParentId, merchantId));
        if (list != null && !list.isEmpty()) {
            return AjaxResult.error("该码商有下级，无法删除");
        }
        //删除用户
        userService.deleteUserById(merchantId);
        //删除码商
        baseMapper.deleteById(merchantId);
        //删除商户码商关联关系
        shopMerchantRelationService.remove(Wrappers.lambdaQuery(ShopMerchantRelationEntity.class).eq(ShopMerchantRelationEntity::getMerchantId, merchantId));
        //删除码商的码
        merchantQrcodeMapper.delete(Wrappers.lambdaQuery(MerchantQrcodeEntity.class).eq(MerchantQrcodeEntity::getMerchantId, merchantId));
        asyncRedisService.asyncReportQrcode();
        return AjaxResult.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult deleteMerchantChild(Long merchantId) {
        //自己
        MerchantEntity merchant = this.getById(merchantId);
        if (ObjectUtil.isEmpty(merchant)) {
            return AjaxResult.error("已被删除");
        }
        //获取自己以及下级
        List<MerchantEntity> list = this.list(Wrappers.lambdaQuery(MerchantEntity.class)
                .likeRight(MerchantEntity::getParentPath, merchant.getParentPath()));
        if (list != null && !list.isEmpty()) {
            List<Long> ids = list.stream().map(MerchantEntity::getUserId).collect(Collectors.toList());
            //删除用户
            userService.deleteUserByIds(list.stream().toArray(Long[]::new));
            //删除码商
            this.removeBatchByIds(ids);
            //删除商户码商关联关系
            shopMerchantRelationService.remove(Wrappers.lambdaQuery(ShopMerchantRelationEntity.class).in(ShopMerchantRelationEntity::getMerchantId, ids));
            asyncRedisService.asyncReportQrcode();

        }
        return null;
    }

    @Override
    public void updateAmount(AmountChangeDTO amountChangeDTO) {
        try {
            String moneyKyes = RedisKeys.merchantBalance + amountChangeDTO.getUserId();
            if (amountChangeDTO.getAmountType() == 3) {
                moneyKyes =RedisKeys.merchantPrepayment + amountChangeDTO.getUserId();
            }
            //redis中处理预付和押金
            boolean redisOpFlag = redisUtils.addMonery(moneyKyes, amountChangeDTO.getChangeAmount().toString());
            if (redisOpFlag) {
                try {
                    BigDecimal afterAmount = new BigDecimal(redisUtils.get(moneyKyes));
                    BigDecimal shopBalance = afterAmount.subtract(amountChangeDTO.getChangeAmount());
                    MerchantAmountRecordsEntity recordsEntity = MerchantAmountRecordsEntity.builder()
                            .userId(amountChangeDTO.getUserId())
                            .userName(amountChangeDTO.getUserName())
                            .changeType(ObjectUtil.isNotEmpty(amountChangeDTO.getChangeType())?amountChangeDTO.getChangeType():1)
                            .amountType(ObjectUtil.isNotEmpty(amountChangeDTO.getAmountType())? amountChangeDTO.getAmountType():1)
                            .beforeAmount(shopBalance)
                            .changeAmount(amountChangeDTO.getChangeAmount())
                            .afterAmount(afterAmount)
                            .createTime(new Date())
                            .notes(amountChangeDTO.getNotes())
                            .remarks(amountChangeDTO.getRemarks())
                            .orderNo(amountChangeDTO.getOrderNo())
                            .agentId(amountChangeDTO.getAgentId())
                            .operator(amountChangeDTO.getOperator())
                            .build();
                    merchantAmountRecordsService.save(recordsEntity);
                    //更新钱包
                    MerchantEntity merchant = MerchantEntity.builder()
                            .userId(amountChangeDTO.getUserId())
                            .build();
                    if (amountChangeDTO.getAmountType() == 1) {
                        merchant.setBalance(afterAmount);
                    } if (amountChangeDTO.getAmountType() == 3) {
                        merchant.setPrepayment(afterAmount);
                    }
                    baseMapper.updateById(merchant);
                }catch (Exception e) {
                    log.error("订单-{}修改码商余额，redis已完成增加，更新账变和钱包时异常：{}", amountChangeDTO.getOrderNo(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("码商变更余额失败-{},系统订单号：{}", e.getMessage(), amountChangeDTO.getOrderNo());
            throw new ServiceException("码商余额操作失败");
        }
    }

    @Override
    public void subAmount(AmountChangeDTO amountChangeDTO) {
        String lockOrderId = RedisKeys.CPAY_ORDER_OP_LOCK+amountChangeDTO.getOrderNo();
        if(redisLock.getLock(lockOrderId,"1")) {
            String moneyKyes = RedisKeys.merchantBalance + amountChangeDTO.getUserId();
            Integer opcoin = 0;
            try {
                //redis中处理预付和押金
                boolean redisOpFlag = redisUtils.addMonery(moneyKyes, amountChangeDTO.getChangeAmount().toString());
                BigDecimal afterAmount = new BigDecimal(redisUtils.get(moneyKyes));
                //获取码商押金
                Map<Long, BigDecimal> poMap = new HashMap<>();
                try {
                    List<MerchantDepositVO> poList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantDeposit), MerchantDepositVO.class);
                    poMap = poList.stream().collect(Collectors.toMap(MerchantDepositVO::getUserId, MerchantDepositVO::getBaseDeposit,(key1,key2)->key2));
                }catch (Exception e) {

                }
                BigDecimal deposit = BigDecimal.ZERO;
                if (ObjectUtil.isNotEmpty(poMap.get(amountChangeDTO.getUserId()))) {
                    deposit = poMap.get(amountChangeDTO.getUserId());
                }
                if (afterAmount.compareTo(deposit) < 0) {
                    //余额不足则将余额加回去，并抛出异常
                    redisUtils.addMonery(moneyKyes, amountChangeDTO.getChangeAmount().multiply(new BigDecimal(-1)).toString());
                    log.error("码商余额不足，对应的码商为：{}", amountChangeDTO.getUserName());
                    throw new ServiceException("码商：" + amountChangeDTO.getUserName() +"的余额不足");
                }
                if (redisOpFlag) {
                    opcoin = 1;
                    BigDecimal shopBalance = afterAmount.subtract(amountChangeDTO.getChangeAmount());
                    MerchantAmountRecordsEntity recordsEntity = MerchantAmountRecordsEntity.builder()
                            .userId(amountChangeDTO.getUserId())
                            .userName(amountChangeDTO.getUserName())
                            .changeType(ObjectUtil.isNotEmpty(amountChangeDTO.getChangeType()) ? amountChangeDTO.getChangeType() : 1)
                            .amountType(ObjectUtil.isNotEmpty(amountChangeDTO.getAmountType()) ? amountChangeDTO.getAmountType() : 1)
                            .beforeAmount(shopBalance)
                            .changeAmount(amountChangeDTO.getChangeAmount())
                            .afterAmount(afterAmount)
                            .createTime(new Date())
                            .notes(amountChangeDTO.getNotes())
                            .remarks(amountChangeDTO.getRemarks())
                            .orderNo(amountChangeDTO.getOrderNo())
                            .agentId(amountChangeDTO.getAgentId())
                            .build();
                    merchantAmountRecordsService.save(recordsEntity);
                    //更新钱包
                    MerchantEntity merchant = MerchantEntity.builder()
                            .userId(amountChangeDTO.getUserId())
                            .build();
                    if (amountChangeDTO.getAmountType() == 1) {
                        merchant.setBalance(afterAmount);
                    }
                    if (amountChangeDTO.getAmountType() == 3) {
                        merchant.setPrepayment(afterAmount);
                    }
                    baseMapper.updateById(merchant);
                }
            } catch (Exception e) {
                e.printStackTrace();
                //余额不足则将余额加回去，并抛出异常
                if (opcoin == 1) {
                    redisUtils.addMonery(moneyKyes, amountChangeDTO.getChangeAmount().multiply(new BigDecimal(-1)).toString());
                }
                log.error("余额扣减失败，{}", e.getMessage());
                throw new ServiceException(e.getMessage());
            } finally {
                redisLock.releaseLock(lockOrderId,"1");
            }
        } else {
            log.error("余额扣减失败");
            throw new ServiceException("订单已被锁定，余额扣减失败");
        }
    }

    @Override
    public List<MerchantDepositVO> listBaseDeposit() {
        return baseMapper.listBaseDeposit();
    }

    @Override
    public List<MerchantEntity> getParentlist(MerchantEntity merchantEntity) {
        if (StrUtil.isNotEmpty(merchantEntity.getParentPath())){
            String[] parentIds = merchantEntity.getParentPath().split("/");
            return this.list(
                    Wrappers.lambdaQuery(MerchantEntity.class)
                            .in(MerchantEntity::getUserId, parentIds)
                            .ne(MerchantEntity::getUserId, merchantEntity.getUserId())
                            .orderByAsc(MerchantEntity::getMerchantLevel)
            );
        }
        return null;
    }

    @Override
    public List<MerchantEntity> listAllChild(List<MerchantEntity> allList, MerchantEntity merchantEntity) {
        List<MerchantEntity> childList = new ArrayList<>();
        allList.forEach(data -> {
            if (!data.getUserId().equals(merchantEntity.getUserId())) {
                String[] paths = data.getParentPath().split("/");
                for (int i = 0; i < paths.length; i++) {
                    String pathId = paths[i];
                    if (pathId.equals(merchantEntity.getUserId().toString())) {
                        data.setCurrLevel(i + 1);
                        childList.add(data);
                    }
                }
            }
        });
        return childList;
    }

    @Override
    public void trimFreezeToBalance(MerchantEntity merchant, BigDecimal amount, String remarks) {
        String balancekey = RedisKeys.merchantBalance + merchant.getUserId();
        String freezeKey = RedisKeys.merchantCommission + merchant.getUserId();
        BigDecimal freezeChange = amount.multiply(new BigDecimal(-1));
        BigDecimal commission = new BigDecimal(redisUtils.get(freezeKey));
        if (commission.compareTo(amount) < 0) {
            throw new ServiceException("转移金额超过佣金");
        }
        boolean freezeFlag = redisUtils.addMonery(freezeKey, freezeChange.toString());
        if (freezeFlag) {
            boolean balanceFlag = redisUtils.addMonery(balancekey, amount.toString());
            if (balanceFlag) {
                try{
                    //创建并保存余额资金记录
                    BigDecimal afterBalance = new BigDecimal(redisUtils.get(balancekey));
                    BigDecimal beforeBalance = afterBalance.subtract(amount);
                    MerchantAmountRecordsEntity recodes = MerchantAmountRecordsEntity.builder()
                            .userId(merchant.getUserId())
                            .userName(merchant.getUserName())
                            .beforeAmount(beforeBalance)
                            .afterAmount(afterBalance)
                            .changeAmount(amount)
                            .amountType(1)//余额
                            .changeType(3)//人工
                            .createTime(new Date())
                            .agentId(merchant.getAgentId())
                            .remarks(remarks)
                            .notes(SecurityUtils.getUsername() + "将佣金转移至余额")
                            .build();
                    merchantAmountRecordsService.save(recodes);
                    //创建并保存佣金记录
                    BigDecimal afterCommission = new BigDecimal(redisUtils.get(freezeKey));
                    BigDecimal beforeCommission = afterCommission.add(amount);
                    MerchantAmountRecordsEntity commissionRecords = MerchantAmountRecordsEntity.builder()
                            .userId(merchant.getUserId())
                            .userName(merchant.getUserName())
                            .beforeAmount(beforeCommission)
                            .afterAmount(afterCommission)
                            .changeAmount(freezeChange)
                            .amountType(2)//佣金
                            .changeType(3)//人工
                            .createTime(new Date())
                            .agentId(merchant.getAgentId())
                            .remarks(remarks)
                            .notes(SecurityUtils.getUsername() + "将佣金转移至余额")
                            .build();
                    merchantAmountRecordsService.save(commissionRecords);
                    //保存至数据库
                    merchant.setFreezeAmount(afterCommission);
                    merchant.setBalance(afterBalance);
                    baseMapper.updateById(merchant);
                }catch (Exception e){
                    e.printStackTrace();
                    //异常时将佣金加回来，将余额减回来
                    redisUtils.addMonery(freezeKey, amount.toString());
                    redisUtils.addMonery(balancekey, freezeChange.toString());
                    log.error("转移积分失败：{}", e.getMessage());
                    throw new ServiceException("转移积分失败");
                }
            } else {
                //增加余额失败时，将押金加回来
                redisUtils.addMonery(freezeKey, amount.toString());
                throw new ServiceException("转移积分失败");
            }
        }
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void addMerchantCommission(InOrderEntity inOrderEntity, InOrderDetailEntity detailEntity) {
        MerchantEntity myInfo = baseMapper.selectById(inOrderEntity.getMerchantId());
        if (myInfo != null && myInfo.getParentId() != null && StrUtil.isNotEmpty(myInfo.getParentPath()) && !StrUtil.startWith(myInfo.getParentPath(), myInfo.getUserId().toString())) {
            String[] parentIds = myInfo.getParentPath().split("/");
            List<String> parentIdList = Arrays.asList(parentIds);
            List<MerchantEntity> list = baseMapper.selectList(
                    Wrappers.lambdaQuery(MerchantEntity.class)
                            .in(MerchantEntity::getUserId, parentIdList)
                            .ne(MerchantEntity::getUserId, myInfo.getUserId())
                            .eq(MerchantEntity::getAgentId, myInfo.getAgentId())
                            .orderByAsc(MerchantEntity::getMerchantLevel)
            );
            //自己的佣金
            BigDecimal selfCommission = inOrderEntity.getOrderAmount().multiply(detailEntity.getMerchantRate()).divide(new BigDecimal(100), 2, RoundingMode.DOWN);
            boolean selfFlag = redisUtils.addMonery(RedisKeys.merchantCommission + myInfo.getUserId(), selfCommission.toString());
            if (selfFlag) {
                BigDecimal afterAmount = new BigDecimal(redisUtils.get(RedisKeys.merchantCommission + myInfo.getUserId()));
                BigDecimal beforeAmount = afterAmount.subtract(selfCommission);
                MerchantAmountRecordsEntity recodes = MerchantAmountRecordsEntity.builder()
                        .userId(myInfo.getUserId())
                        .userName(myInfo.getUserName())
                        .beforeAmount(beforeAmount)
                        .afterAmount(afterAmount)
                        .changeAmount(selfCommission)
                        .amountType(2)//佣金
                        .changeType(1)//非人工
                        .createTime(new Date())
                        .agentId(myInfo.getAgentId())
                        .orderNo(inOrderEntity.getTradeNo())
                        .remarks("订单【" + inOrderEntity.getTradeNo() + "】确认收款，佣金：" + selfCommission.setScale(2, RoundingMode.DOWN) )
                        .build();
                merchantAmountRecordsService.save(recodes);
                myInfo.setFreezeAmount(afterAmount);
                baseMapper.updateById(myInfo);
            }
            if (selfFlag && !list.isEmpty()) {
                List<MerchantAmountRecordsEntity> recordsList = new ArrayList<>();
                for (MerchantEntity parent : list) {
                    String commissionKey = RedisKeys.merchantCommission + parent.getUserId();
                    BigDecimal commissionRate = BigDecimal.ZERO;
                    StringBuilder remarks = new StringBuilder("订单【");
                    remarks.append(inOrderEntity.getTradeNo()).append("】确认收款，");
                    if (parent.getMerchantLevel() == 1) {
                        commissionRate = detailEntity.getMerchantRateOne().subtract(detailEntity.getMerchantRateTwo());
                        remarks.append("费率差为：").append(commissionRate.setScale(2, RoundingMode.DOWN));
                    } else if (parent.getMerchantLevel() == 2) {
                        commissionRate = detailEntity.getMerchantRateTwo().subtract(detailEntity.getMerchantRateThree());
                        remarks.append("费率差为：").append(commissionRate.setScale(2, RoundingMode.DOWN));
                    } else if (parent.getMerchantLevel() == 3) {
                        commissionRate = detailEntity.getMerchantRateThree().subtract(detailEntity.getMerchantRateFour());
                        remarks.append("费率差为：").append(commissionRate.setScale(2, RoundingMode.DOWN));
                    } else if (parent.getMerchantLevel() == 4) {
                        commissionRate = detailEntity.getMerchantRateFour().subtract(detailEntity.getMerchantRateFive());
                        remarks.append("费率差为：").append(commissionRate.setScale(2, RoundingMode.DOWN));
                    }
                    //需要增加的金额为：订单金额*费率差
                    BigDecimal changeAmount = inOrderEntity.getOrderAmount().multiply(commissionRate).divide(new BigDecimal(100),2, RoundingMode.DOWN);
                    if (changeAmount.compareTo(BigDecimal.ZERO) > 0) {
                        boolean flag = redisUtils.addMonery(commissionKey, changeAmount.toString());
                        if (flag) {
                            BigDecimal afterAmount = new BigDecimal(redisUtils.get(commissionKey));
                            parent.setFreezeAmount(afterAmount);
                            BigDecimal beforeAmount = afterAmount.subtract(changeAmount);
                            MerchantAmountRecordsEntity recodes = MerchantAmountRecordsEntity.builder()
                                    .userId(parent.getUserId())
                                    .userName(parent.getUserName())
                                    .beforeAmount(beforeAmount)
                                    .afterAmount(afterAmount)
                                    .changeAmount(changeAmount)
                                    .amountType(2)//佣金
                                    .changeType(1)//非人工
                                    .createTime(new Date())
                                    .agentId(parent.getAgentId())
                                    .orderNo(inOrderEntity.getTradeNo())
                                    .remarks(remarks.toString())
                                    .build();
                            recordsList.add(recodes);
                            parent.setFreezeAmount(afterAmount);
                        }
                    }
                }
                merchantAmountRecordsService.saveBatch(recordsList);
                this.updateBatchById(list);
            }
        }else if (myInfo != null && myInfo.getParentId() != null && StrUtil.isNotEmpty(myInfo.getParentPath())){
            //自己的佣金
            BigDecimal selfCommission = inOrderEntity.getOrderAmount().multiply(detailEntity.getMerchantRate()).divide(new BigDecimal(100), 2, RoundingMode.DOWN);
            boolean selfFlag = redisUtils.addMonery(RedisKeys.merchantCommission + myInfo.getUserId(), selfCommission.toString());
            if (selfFlag) {
                BigDecimal afterAmount = new BigDecimal(redisUtils.get(RedisKeys.merchantCommission + myInfo.getUserId()));
                BigDecimal beforeAmount = afterAmount.subtract(selfCommission);
                MerchantAmountRecordsEntity recodes = MerchantAmountRecordsEntity.builder()
                        .userId(myInfo.getUserId())
                        .userName(myInfo.getUserName())
                        .beforeAmount(beforeAmount)
                        .afterAmount(afterAmount)
                        .changeAmount(selfCommission)
                        .amountType(2)//佣金
                        .changeType(1)//非人工
                        .createTime(new Date())
                        .agentId(myInfo.getAgentId())
                        .orderNo(inOrderEntity.getTradeNo())
                        .remarks("订单【" + inOrderEntity.getTradeNo() + "】确认收款，佣金：" + selfCommission.setScale(2, RoundingMode.DOWN) )
                        .build();
                merchantAmountRecordsService.save(recodes);
                myInfo.setFreezeAmount(afterAmount);
                baseMapper.updateById(myInfo);
            }
        }
    }

    @Override
    public AjaxResult onekeyClearRate(Long channelId) {
        List<MerchantChannelEntity> list = baseMapper.merchantChannelList(SecurityUtils.getUserId(), channelId);
        if (list != null && !list.isEmpty()) {
            list.forEach(data -> {
                data.setChannelRate(BigDecimal.ZERO);
                data.setMerchantRateTwo(BigDecimal.ZERO);
                data.setMerchantRateThree(BigDecimal.ZERO);
                data.setMerchantRateFour(BigDecimal.ZERO);
                data.setMerchantRateFive(BigDecimal.ZERO);
            });
            merchantChannelService.updateBatchById(list);
            asyncRedisService.asyncReportQrcode();
        }
        return AjaxResult.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void subMerchantCommission(String tradeNo) {
        List<MerchantAmountRecordsEntity> list = merchantAmountRecordsService.list(
                Wrappers.lambdaQuery(MerchantAmountRecordsEntity.class)
                        .eq(MerchantAmountRecordsEntity::getOrderNo, tradeNo)
                        .eq(MerchantAmountRecordsEntity::getAmountType, 2)
                        .eq(MerchantAmountRecordsEntity::getChangeType, 1)
        );
        if (list != null && !list.isEmpty()) {
            List<MerchantAmountRecordsEntity> recordsList = new ArrayList<>();
            List<MerchantEntity> updateList = new ArrayList<>();
            for (MerchantAmountRecordsEntity amountRecords : list) {
                String commissionKey = RedisKeys.merchantCommission + amountRecords.getUserId();
                BigDecimal changeAmount = amountRecords.getChangeAmount().multiply(BigDecimal.valueOf(-1));
                boolean flag = redisUtils.addMonery(commissionKey, changeAmount.toString());
                if (flag) {
                    BigDecimal afterAmount = new BigDecimal(redisUtils.get(commissionKey));
                    BigDecimal beforeAmount = afterAmount.subtract(changeAmount);
                    MerchantAmountRecordsEntity recodes = MerchantAmountRecordsEntity.builder()
                            .userId(amountRecords.getUserId())
                            .userName(amountRecords.getUserName())
                            .beforeAmount(beforeAmount)
                            .afterAmount(afterAmount)
                            .changeAmount(changeAmount)
                            .amountType(2)//佣金
                            .changeType(4)//非人工
                            .createTime(new Date())
                            .agentId(amountRecords.getAgentId())
                            .orderNo(tradeNo)
                            .remarks("订单冲正")
                            .build();
                    recordsList.add(recodes);
                    updateList.add(MerchantEntity.builder()
                                    .userId(amountRecords.getUserId())
                                    .freezeAmount(afterAmount)
                            .build());
                }
            }
            if (!recordsList.isEmpty()) {
                merchantAmountRecordsService.saveBatch(recordsList);
            }
            if (!updateList.isEmpty()) {
                this.updateBatchById(updateList);
            }
        }
    }

    @Override
    public void updateAmountToMerchant(MerchantEntity merchantUserEntity, MerchantEntity merchantEntity, BigDecimal changeAmount, String remark) {
        String balanceUserKey = RedisKeys.merchantBalance + merchantUserEntity.getUserId();
        BigDecimal commission = new BigDecimal(redisUtils.get(balanceUserKey));
        if (commission.compareTo(changeAmount) < 0) {
            throw new ServiceException("转移金额超过余额");
        }
        boolean balanceUserFlag = redisUtils.payMonery(balanceUserKey, changeAmount.toString());
        if (balanceUserFlag) {
            try{
                //创建并保存余额资金记录
                BigDecimal afterBalance = new BigDecimal(redisUtils.get(balanceUserKey));
                BigDecimal beforeBalance = afterBalance.add(changeAmount);
                MerchantAmountRecordsEntity recodes = MerchantAmountRecordsEntity.builder()
                        .userId(merchantUserEntity.getUserId())
                        .userName(merchantUserEntity.getUserName())
                        .beforeAmount(beforeBalance)
                        .afterAmount(afterBalance)
                        .changeAmount(changeAmount)
                        .amountType(1)//余额
                        .changeType(3)//人工
                        .createTime(new Date())
                        .agentId(merchantUserEntity.getAgentId())
                        .remarks(remark)
                        .notes(SecurityUtils.getUsername() + "将余额"+changeAmount+"转移给"+merchantEntity.getMerchantName())
                        .build();
                merchantAmountRecordsService.save(recodes);

                //保存至数据库
                merchantUserEntity.setBalance(afterBalance);
                baseMapper.updateById(merchantUserEntity);
            }catch (Exception e){
                e.printStackTrace();
                //异常时将佣金加回来，将余额减回来
                redisUtils.addMonery(balanceUserKey, changeAmount.toString());
                log.error("转移积分失败：{}", e.getMessage());
                throw new ServiceException("转移积分失败");
            }
        }

        String balanceKey = RedisKeys.merchantBalance + merchantEntity.getUserId();
        boolean balanceFlag = redisUtils.addMonery(balanceKey, changeAmount.toString());
        if (balanceFlag) {
            try{
                //创建并保存余额资金记录
                BigDecimal afterBalance = new BigDecimal(redisUtils.get(balanceUserKey));
                BigDecimal beforeBalance = afterBalance.subtract(changeAmount);
                MerchantAmountRecordsEntity recodes = MerchantAmountRecordsEntity.builder()
                        .userId(merchantEntity.getUserId())
                        .userName(merchantEntity.getUserName())
                        .beforeAmount(beforeBalance)
                        .afterAmount(afterBalance)
                        .changeAmount(changeAmount)
                        .amountType(1)//余额
                        .changeType(3)//人工
                        .createTime(new Date())
                        .agentId(merchantEntity.getAgentId())
                        .remarks(remark)
                        .notes( merchantEntity.getMerchantName()+ "获得"+SecurityUtils.getUsername()+"转移的余额"+changeAmount)
                        .build();
                merchantAmountRecordsService.save(recodes);

                //保存至数据库
                merchantEntity.setBalance(afterBalance);
                baseMapper.updateById(merchantEntity);
            }catch (Exception e){
                e.printStackTrace();
                //异常时将佣金加回来，将余额减回来
                redisUtils.payMonery(balanceUserKey, changeAmount.toString());
                log.error("转移积分失败：{}", e.getMessage());
                throw new ServiceException("转移积分失败");
            }
        }

    }
}
