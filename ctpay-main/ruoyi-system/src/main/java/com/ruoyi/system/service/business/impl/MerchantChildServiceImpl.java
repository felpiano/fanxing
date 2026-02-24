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
import com.ruoyi.common.enums.SysRoleEnum;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.RedisLock;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.business.*;
import com.ruoyi.system.domain.dto.MerchantChildSaveDTO;
import com.ruoyi.system.domain.vo.MerchantDepositVO;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.business.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j(topic = "ct-business")
@Service
public class MerchantChildServiceImpl implements MerchantChildService {
    @Resource
    private MerchantService merchantService;
    @Resource
    private ISysUserService userService;
    @Resource
    private ISysRoleService roleService;
    @Resource
    private MerchantAmountRecordsService merchantAmountRecordsService;
    @Resource
    private MerchantChannelService merchantChannelService;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private RedisLock redisLock;
    @Resource
    private ShopMerchantRelationService shopMerchantRelationService;
    @Resource
    private AsyncRedisService asyncRedisService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult addChildMerchant(String loginName, String nickName, String password) {
        MerchantEntity parentMerchant = merchantService.getById(SecurityUtils.getUserId());
        SysUser newUser = new SysUser();
        newUser.setUserName(loginName);
        if (!userService.checkUserNameUnique(newUser)) {
            return AjaxResult.error("新增码商'" + loginName + "'失败，登录账号已存在");
        }
        SysUser loginUser = SecurityUtils.getLoginUser().getUser();
        newUser.setNickName(nickName);
        newUser.setCreateBy(loginUser.getUserName());
        newUser.setPassword(SecurityUtils.encryptPassword(password));
        //设置用户角色为码商
        SysRole sysRole = roleService.getRoleByRoleKey(SysRoleEnum.MERCHANT.getRoleKey());
        Long[] roleIds = {sysRole.getRoleId()};
        newUser.setRoleIds(roleIds);
        //设置用户其他属性
        newUser.setIdentity((int)SysRoleEnum.MERCHANT.getRoleId());
        newUser.setGoogleSecretFlag(0);
        newUser.setUid(loginUser.getUid());
        //判断层级层数
        if (StrUtil.isNotEmpty(parentMerchant.getParentPath()) && parentMerchant.getParentPath().split("/").length == 5) {
            return AjaxResult.error("层级已超出限制");
        }
        //保存用户
        boolean flag = userService.insertUser(newUser) > 0;
        //设置下级码商
        MerchantEntity merchant = BeanUtil.copyProperties(parentMerchant, MerchantEntity.class);
        merchant.setUserName(loginName);
        merchant.setMerchantName(loginName);
        merchant.setAgentId(parentMerchant.getAgentId());
        merchant.setUserId(newUser.getUserId());
        merchant.setParentId(parentMerchant.getUserId());
        merchant.setParentName(parentMerchant.getMerchantName());
        merchant.setSafeCode(null);
        merchant.setWhiteCallbackIp(null);
        merchant.setAllowLoginIp(null);
        merchant.setTelegram(null);
        merchant.setBalance(BigDecimal.ZERO);
        merchant.setPrepayment(BigDecimal.ZERO);
        merchant.setFreezeAmount(BigDecimal.ZERO);
        merchant.setTelegramGroup(null);
        merchant.setCreateTime(new Date());
        merchant.setUpdateTime(new Date());
        merchant.setParentPath(StrUtil.isEmpty(parentMerchant.getParentPath())
                ?   parentMerchant.getUserId() + "/" :
                parentMerchant.getParentPath()  +merchant.getUserId() + "/");
        String[] levels = merchant.getParentPath().split("/");
        merchant.setMerchantLevel(levels.length);
        flag = flag && merchantService.save(merchant);
        //保存码商基础通道
        List<MerchantChannelEntity> parentChannelList = merchantChannelService.list(
                Wrappers.lambdaQuery(MerchantChannelEntity.class)
                        .eq(MerchantChannelEntity::getMerchantId, parentMerchant.getUserId())
        );
        if (parentChannelList != null && !parentChannelList.isEmpty()) {
            List<MerchantChannelEntity> merChannelList = BeanUtil.copyToList(parentChannelList, MerchantChannelEntity.class);
            merChannelList.forEach(data -> {
                data.setId(null);
                data.setMerchantId(merchant.getUserId());
                switch (merchant.getMerchantLevel()){
                    case 2: data.setMerchantRateOne(data.getChannelRate()); break;
                    case 3: data.setMerchantRateTwo(data.getChannelRate()); break;
                    case 4: data.setMerchantRateThree(data.getChannelRate()); break;
                    case 5: data.setMerchantRateFour(data.getChannelRate()); break;
                }
                //增加下级码商时，费率默认为0
                data.setChannelRate(BigDecimal.ZERO);
            });
            merchantChannelService.saveBatch(merChannelList);
        }
        //保存码商与商户关联关系
        List<ShopMerchantRelationEntity> parentRelations = shopMerchantRelationService.list(
                Wrappers.lambdaQuery(ShopMerchantRelationEntity.class)
                        .eq(ShopMerchantRelationEntity::getMerchantId, parentMerchant.getUserId())
        );
        if (parentRelations != null && !parentRelations.isEmpty()) {
            List<ShopMerchantRelationEntity> childRelations = BeanUtil.copyToList(parentRelations, ShopMerchantRelationEntity.class);
            childRelations.forEach(data -> {
                data.setId(null);
                data.setMerchantId(merchant.getUserId());
            });
            shopMerchantRelationService.saveBatch(childRelations);
        }
        //将余额和冻结金额保存至redis
        redisUtils.set(RedisKeys.merchantBalance + merchant.getUserId(), ObjectUtil.isNotEmpty(merchant.getBalance())? merchant.getBalance().toString():"0");
        redisUtils.set(RedisKeys.merchantPrepayment + merchant.getUserId(), ObjectUtil.isNotEmpty(merchant.getPrepayment())? merchant.getPrepayment().toString():"0");
        asyncRedisService.asyncReportQrcode();
        return flag? AjaxResult.success():AjaxResult.error();
    }

    @Override
    public AjaxResult trimBalance(Long childId, BigDecimal changeAmount,String remark) {
        MerchantEntity childMerchant = merchantService.getById(childId);
        MerchantEntity parentMerchant = merchantService.getById(SecurityUtils.getUserId());
        if (childMerchant == null || !childMerchant.getParentPath().startsWith(parentMerchant.getParentPath())) {
            return AjaxResult.error("仅能转移金额给自己的下级");
        }
        String selfKey = RedisKeys.merchantBalance + parentMerchant.getUserId();
        String childKey = RedisKeys.merchantBalance + childId;
        //判断余额是否足够
        boolean flag = redisLock.getLock(RedisKeys.merchantBalanceLocked + parentMerchant.getUserId(), "1");
        if (flag) {
            try {
                //获取押金
                List<MerchantDepositVO> poList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantDeposit), MerchantDepositVO.class);
                Map<Long, BigDecimal> poMap = poList.stream().collect(Collectors.toMap(MerchantDepositVO::getUserId, MerchantDepositVO::getBaseDeposit,(key1, key2)->key2));
                BigDecimal yj = ObjectUtil.isNotEmpty(poMap.get(parentMerchant.getUserId()))?poMap.get(parentMerchant.getUserId()):BigDecimal.ZERO;
                if (new BigDecimal(redisUtils.get(selfKey)).subtract(yj).compareTo(changeAmount) >= 0) {
                    //给自己减金额
                    //redis中处理预付和押金
                    BigDecimal selfChangeAmount = changeAmount.multiply(new BigDecimal(-1));
                    String parentNotes = "给下级" + childMerchant.getUserName() + "转移余额：" + changeAmount;
                    addMoney(selfChangeAmount, selfKey, parentMerchant, remark, parentNotes);
                    //给下级加余额
                    String childNotes = "上级" + parentMerchant.getUserName() + "转移余额：" + changeAmount;
                    addMoney(changeAmount, childKey, childMerchant, remark, childNotes);
                } else {
                    return AjaxResult.error("余额不足");
                }
            }catch (Exception e){
                log.error("转移余额异常");
                return AjaxResult.error("系统繁忙，请稍后重试");
            } finally {
                redisLock.releaseLock(RedisKeys.merchantBalanceLocked + parentMerchant.getUserId(), "1");
            }
        }
        return AjaxResult.success();
    }

    @Override
    public AjaxResult updateChildMerchant(MerchantChildSaveDTO saveDTO) {
        MerchantEntity parentMerchant = merchantService.getById(SecurityUtils.getUserId());
        MerchantEntity childMerchant = merchantService.getById(saveDTO.getUserId());
        if (!childMerchant.getParentPath().startsWith(parentMerchant.getParentPath())) {
            return AjaxResult.error("非法访问");
        }
        MerchantEntity merchant = BeanUtil.copyProperties(saveDTO, MerchantEntity.class);
        SysUser sysUser = userService.selectUserById(merchant.getUserId());
        if (StrUtil.isNotEmpty(merchant.getLoginPassword())) {
            sysUser.setPassword(SecurityUtils.encryptPassword(merchant.getLoginPassword()));
            merchant.setLoginPassword(sysUser.getPassword());
            userService.updateOnlyUser(sysUser);
        }
        if (StrUtil.isNotEmpty(merchant.getSafeCode())) {
            merchant.setSafeCode(SecurityUtils.encryptPassword(merchant.getSafeCode()));
        }
        boolean flag = merchantService.updateById(merchant);
        asyncRedisService.asyncReportQrcode();
        return flag ? AjaxResult.success():AjaxResult.error();
    }

    @Override
    public AjaxResult trimBalanceFree(Long toId, BigDecimal changeAmount, String remark) {
        MerchantEntity toMerchant = merchantService.getById(toId);
        if (ObjectUtil.isNull(toMerchant)) {
            return AjaxResult.error("转移对象不存在");
        }
        MerchantEntity self = merchantService.getById(SecurityUtils.getUserId());
        if (!toMerchant.getAgentId().equals(self.getAgentId())) {
            return AjaxResult.error("转移对象不存在");
        }
        if (toMerchant.getUserId().equals(self.getUserId())) {
            return AjaxResult.error("不能给自己转移");
        }
        String selfKey = RedisKeys.merchantBalance + self.getUserId();
        String childKey = RedisKeys.merchantBalance + toMerchant.getUserId();
        //判断余额是否足够
        boolean flag = redisLock.getLock(RedisKeys.merchantBalanceLocked + self.getUserId(), "1");
        if (flag) {
            try {
                //获取押金
                List<MerchantDepositVO> poList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantDeposit), MerchantDepositVO.class);
                Map<Long, BigDecimal> poMap = poList.stream().collect(Collectors.toMap(MerchantDepositVO::getUserId, MerchantDepositVO::getBaseDeposit,(key1, key2)->key2));
                BigDecimal yj = ObjectUtil.isNotEmpty(poMap.get(self.getUserId()))?poMap.get(self.getUserId()):BigDecimal.ZERO;
                if (new BigDecimal(redisUtils.get(selfKey)).subtract(yj).compareTo(changeAmount) >= 0) {
                    //给自己减金额
                    BigDecimal selfChangeAmount = changeAmount.multiply(new BigDecimal(-1));
                    String parentNotes = "给码商" + toMerchant.getUserName() + "转移余额";
                    addMoney(selfChangeAmount, selfKey, self, remark, parentNotes);
                    //给转移对象加余额
                    String childNotes = "码商" + self.getUserName() + "给我转移余额";
                    addMoney(changeAmount, childKey, toMerchant, remark, childNotes);
                } else {
                    return AjaxResult.error("您的余额不足");
                }
            }catch (Exception e){
                log.error("转移余额异常:{}", e.getMessage());
                return AjaxResult.error("系统繁忙，请稍后重试");
            } finally {
                redisLock.releaseLock(RedisKeys.merchantBalanceLocked + self.getUserId(), "1");
            }
        }
        return AjaxResult.success();
    }

    private void addMoney(BigDecimal changeAmount,String redisKey, MerchantEntity merchant, String remark, String notes){
        boolean redisOpFlag = redisUtils.addMonery(redisKey, changeAmount.toString());
        if (redisOpFlag) {
            BigDecimal afterAmount = new BigDecimal(redisUtils.get(redisKey));
            BigDecimal beforeAmount = afterAmount.subtract(changeAmount);
            //资金记录
            MerchantAmountRecordsEntity recordsEntity = MerchantAmountRecordsEntity.builder()
                    .userId(merchant.getUserId())
                    .userName(merchant.getUserName())
                    .changeType(3)
                    .amountType(1)
                    .beforeAmount(beforeAmount)
                    .changeAmount(changeAmount)
                    .afterAmount(afterAmount)
                    .createTime(new Date())
                    .notes(notes)
                    .remarks(remark)
                    .orderNo("")
                    .operator(SecurityUtils.getUsername())
                    .agentId(merchant.getAgentId())
                    .build();
            merchantAmountRecordsService.save(recordsEntity);
            //更新钱包
            MerchantEntity updateMerchant = MerchantEntity.builder()
                    .userId(merchant.getUserId())
                    .balance(afterAmount)
                    .build();
            merchantService.updateById(updateMerchant);
        }
    }
}
