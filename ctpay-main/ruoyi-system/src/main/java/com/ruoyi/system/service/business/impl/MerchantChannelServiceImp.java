package com.ruoyi.system.service.business.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.system.domain.business.MerchantChannelEntity;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.ruoyi.system.domain.vo.MerchantDepositVO;
import com.ruoyi.system.mapper.business.MerchantChannelMapper;
import com.ruoyi.system.mapper.business.MerchantMapper;
import com.ruoyi.system.service.business.MerchantChannelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 码商通道费率 服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
@Service
public class MerchantChannelServiceImp extends ServiceImpl<MerchantChannelMapper, MerchantChannelEntity> implements MerchantChannelService {

    @Resource
    private MerchantMapper merchantMapper;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private AsyncRedisService asyncRedisService;
    @Autowired
    private MerchantChannelMapper merchantChannelMapper;

    @Override
    public List<MerchantChannelEntity> listByMerchantId(Long merchantId, Long channelId) {
        return baseMapper.listByMerchantId(merchantId, channelId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMerchantChannelBatch(List<MerchantChannelEntity> list) {
        //根据通道获取map
        Map<Long, MerchantChannelEntity> parentMap = list.stream().collect(Collectors.toMap(MerchantChannelEntity::getChannelId, a -> a));
        //获取当前码商
        MerchantEntity merchantEntity = merchantMapper.selectById(list.get(0).getMerchantId());
        //获取所有下级码商,包含自己
        List<MerchantEntity> childList = listAllChild(merchantEntity);
        if (!childList.isEmpty()) {
            Map<Long, MerchantEntity> merchantMap = childList.stream().collect(Collectors.toMap(MerchantEntity::getUserId, a -> a));
            //获取所有下级码商的通道
            List<MerchantChannelEntity> allList = this.list(Wrappers.lambdaQuery(MerchantChannelEntity.class)
                    .in(MerchantChannelEntity::getMerchantId, childList.stream().map(MerchantEntity::getUserId).collect(Collectors.toList())));
            allList.forEach(data -> {
                //更新的通道
                MerchantChannelEntity parentChannel = parentMap.get(data.getChannelId());
                //父级码商
                if (parentChannel != null) {
                    MerchantEntity parent = merchantMap.get(data.getMerchantId());
                    //同步超时时长和押金
                    data.setMerchantOvertime(parentChannel.getMerchantOvertime());
                    data.setBaseDeposit(parentChannel.getBaseDeposit());
                    data.setMaxAmount(parentChannel.getMaxAmount());
                    data.setMinAmount(parentChannel.getMinAmount());
                    //父码商在当前码商的层级，修改不同费率
                    switch (parent.getCurrLevel()) {
                        case 1: data.setMerchantRateOne(parentChannel.getChannelRate());break;
                        case 2: data.setMerchantRateTwo(parentChannel.getChannelRate());break;
                        case 3: data.setMerchantRateThree(parentChannel.getChannelRate());break;
                        case 4: data.setMerchantRateFour(parentChannel.getChannelRate());break;
                        case 5: data.setMerchantRateFive(parentChannel.getChannelRate());break;
                        default: break;
                    }
                }

            });
            allList.addAll(list);
            this.updateBatchById(allList);
        } else {
            this.updateBatchById(list);
        }
        List<MerchantDepositVO> merchantDeposits = merchantMapper.listBaseDeposit();
        if (merchantDeposits != null && !merchantDeposits.isEmpty()){
            redisUtils.set(RedisKeys.merchantDeposit, JSONUtil.toJsonStr(merchantDeposits));
        }
        asyncRedisService.asyncReportQrcode();
    }

    @Override
    public List<MerchantEntity> listAllChild(MerchantEntity merchantEntity) {
        List<MerchantEntity> allList = merchantMapper.selectList(Wrappers.<MerchantEntity>lambdaQuery());
        List<MerchantEntity> childList = new ArrayList<>();
        allList.forEach(data -> {
            String[] paths = data.getParentPath().split("/");
            for (int i = 0; i < paths.length; i++) {
                String pathId = paths[i];
                if (pathId.equals(merchantEntity.getUserId().toString())) {
                    data.setCurrLevel(i + 1);
                    childList.add(data);
                }
            }
        });
        return childList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMerchantChannel(MerchantChannelEntity merchantChannel) {
        //获取当前码商
        MerchantEntity merchantEntity = merchantMapper.selectById(merchantChannel.getMerchantId());
        if (merchantEntity.getParentId() != null) {
            //获取父亲码商
            MerchantEntity parentMerchant = merchantMapper.selectById(merchantEntity.getParentId());
            if (parentMerchant != null) {
                MerchantChannelEntity parentChannel = merchantChannelMapper.selectOne(Wrappers.lambdaQuery(MerchantChannelEntity.class)
                        .eq(MerchantChannelEntity::getChannelId, merchantChannel.getChannelId())
                        .eq(MerchantChannelEntity::getMerchantId, parentMerchant.getUserId()));
                if (parentChannel != null && parentChannel.getChannelRate().compareTo(merchantChannel.getChannelRate()) < 0) {
                    throw new ServiceException("费率不能超过父级["+parentMerchant.getUserName() + "]的费率：" + merchantChannel.getChannelRate());
                }
            }
        }
        //获取子码商
        List<MerchantEntity> myChildList = merchantMapper.selectList(Wrappers.lambdaQuery(MerchantEntity.class)
                .eq(MerchantEntity::getParentId, merchantEntity.getUserId()));
        if (myChildList != null && !myChildList.isEmpty()) {
            List<MerchantChannelEntity> myChildChannels = merchantChannelMapper.selectList(Wrappers.lambdaQuery(MerchantChannelEntity.class)
                    .eq(MerchantChannelEntity::getChannelId, merchantChannel.getChannelId())
                            .in(MerchantChannelEntity::getMerchantId,  myChildList.stream().map(MerchantEntity::getUserId).collect(Collectors.toList()))
                    );
            if (myChildChannels != null && !myChildChannels.isEmpty()) {
                BigDecimal maxChannelRate = myChildChannels.stream().max(Comparator.comparing(MerchantChannelEntity::getChannelRate)).get().getChannelRate();
                if (maxChannelRate.compareTo(merchantChannel.getChannelRate()) > 0) {
                    throw new ServiceException("不能低于下级码商的费率:" + maxChannelRate);
                }
            }
        }

        //获取所有下级码商,包含自己
        List<MerchantEntity> childList = listAllChild(merchantEntity);
        if (!childList.isEmpty()) {
            Map<Long, MerchantEntity> merchantMap = childList.stream().collect(Collectors.toMap(MerchantEntity::getUserId, a -> a));
            //获取所有下级码商的通道
            List<MerchantChannelEntity> allList = this.list(Wrappers.lambdaQuery(MerchantChannelEntity.class)
                    .eq(MerchantChannelEntity::getChannelId, merchantChannel.getChannelId())
                    .in(MerchantChannelEntity::getMerchantId, childList.stream().map(MerchantEntity::getUserId).collect(Collectors.toList())));
            allList.forEach(data -> {
                MerchantEntity parent = merchantMap.get(data.getMerchantId());
                //同步设置押金
                data.setBaseDeposit(merchantChannel.getBaseDeposit());
                //父码商在当前码商的层级，修改不同费率
                switch (parent.getCurrLevel()) {
                    case 1: data.setMerchantRateOne(merchantChannel.getChannelRate());break;
                    case 2: data.setMerchantRateTwo(merchantChannel.getChannelRate());break;
                    case 3: data.setMerchantRateThree(merchantChannel.getChannelRate());break;
                    case 4: data.setMerchantRateFour(merchantChannel.getChannelRate());break;
                    case 5: data.setMerchantRateFive(merchantChannel.getChannelRate());break;
                    default: break;
                }
                //如果是自己，则还需要修改当前费率
                if (data.getMerchantId().equals(merchantEntity.getUserId())) {
                    data.setChannelRate(merchantChannel.getChannelRate());
                }
            });
            this.updateBatchById(allList);
        } else {
            this.updateById(merchantChannel);
        }
        List<MerchantDepositVO> merchantDeposits = merchantMapper.listBaseDeposit();
        if (merchantDeposits != null && !merchantDeposits.isEmpty()){
            redisUtils.set(RedisKeys.merchantDeposit, JSONUtil.toJsonStr(merchantDeposits));
        }
        asyncRedisService.asyncReportQrcode();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMerchantChannelList(List<MerchantChannelEntity> list, Integer currLevel, BigDecimal channelRate) {
        if (list == null || list.isEmpty()) {
            throw new ServiceException("参数不能为空");
        }
        Long channelId = list.get(0).getChannelId();
        List<Long> merchantIds = list.stream().map(MerchantChannelEntity::getMerchantId).collect(Collectors.toList());
        Set<Long> merchantIdSet = new HashSet<>(merchantIds);


        // ===== 通过parentPath查找所有下级码商（不含自己） =====
        List<MerchantEntity> descendantList = queryByParentIds(merchantIds);
        Set<Long> descendantIds = descendantList.stream()
                .map(MerchantEntity::getUserId)
                .filter(id -> !merchantIdSet.contains(id))
                .collect(Collectors.toSet());
        Map<Long, String> merchantLevelMap = descendantList.stream()
                .collect(Collectors.toMap(
                        MerchantEntity::getUserId,                      // key: name
                        descendant -> String.valueOf(descendant.getMerchantLevel()),
                        (oldValue, newValue) -> newValue
                ));

        // ===== 批量更新 =====
        List<MerchantChannelEntity> allMerchantChannels = new ArrayList<>();

        // 1. 更新input码商: channelRate + merchantRate[currLevel]
        list.forEach(inputChannel -> {
            setRateByLevel(inputChannel, currLevel, channelRate);
            inputChannel.setChannelRate(channelRate);
            allMerchantChannels.add(inputChannel);
        });

        // 2. 更新所有下级码商: 只修改 merchantRate[currLevel]
        if (!descendantIds.isEmpty()) {
            List<MerchantChannelEntity> descendantChannelRecords = this.list(
                    Wrappers.lambdaQuery(MerchantChannelEntity.class)
                            .eq(MerchantChannelEntity::getChannelId, channelId)
                            .in(MerchantChannelEntity::getMerchantId, descendantIds));
            descendantChannelRecords.forEach(data -> {
                String merchantLevel = merchantLevelMap.get(data.getMerchantId());
                setRateByLevel(data, Integer.parseInt(merchantLevel), channelRate);
            });
            allMerchantChannels.addAll(descendantChannelRecords);
        }

        this.updateBatchById(allMerchantChannels);
        // 刷新Redis缓存
        List<MerchantDepositVO> merchantDeposits = merchantMapper.listBaseDeposit();
        if (merchantDeposits != null && !merchantDeposits.isEmpty()) {
            redisUtils.set(RedisKeys.merchantDeposit, JSONUtil.toJsonStr(merchantDeposits));
        }
        asyncRedisService.asyncReportQrcode();
    }

    public List<MerchantEntity> queryByParentIds(List<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> {
            boolean first = true;
            for (Long id : parentIds) {
                // 中间/末尾匹配: parentPath 包含 /id/
                String patternMiddle = "/" + id + "/";
                // 开头匹配: parentPath 以 id/ 开头（针对第一层级码商）
                String patternStart = id + "/";
                if (first) {
                    wrapper.like(MerchantEntity::getParentPath, patternMiddle)
                            .or().likeRight(MerchantEntity::getParentPath, patternStart);
                    first = false;
                } else {
                    wrapper.or().like(MerchantEntity::getParentPath, patternMiddle)
                            .or().likeRight(MerchantEntity::getParentPath, patternStart);
                }
            }
        });

        return merchantMapper.selectList(queryWrapper);
    }

    /**
     * 根据层级获取对应的费率字段值
     * level 0 → channelRate
     * level 1 → merchantRateOne
     * level 2 → merchantRateTwo
     * ...
     */
    private BigDecimal getRateByLevel(MerchantChannelEntity channel, int level) {
        switch (level) {
            case 0: return channel.getChannelRate();
            case 1: return channel.getMerchantRateOne();
            case 2: return channel.getMerchantRateTwo();
            case 3: return channel.getMerchantRateThree();
            case 4: return channel.getMerchantRateFour();
            case 5: return channel.getMerchantRateFive();
            default: return null;
        }
    }

    /**
     * 根据层级设置对应的费率字段值
     */
    private void setRateByLevel(MerchantChannelEntity channel, int level, BigDecimal rate) {
        switch (level) {
            case 1: channel.setMerchantRateOne(rate); break;
            case 2: channel.setMerchantRateTwo(rate); break;
            case 3: channel.setMerchantRateThree(rate); break;
            case 4: channel.setMerchantRateFour(rate); break;
            case 5: channel.setMerchantRateFive(rate); break;
            default: break;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agentContrl(Long merchantId, Long channelId, Integer agentContrl) {
        MerchantEntity merchant = merchantMapper.selectById(merchantId);
        List<MerchantEntity> childList = listAllChild(merchant);
        List<MerchantChannelEntity> owerChannelList = this.list(
                Wrappers.lambdaQuery(MerchantChannelEntity.class)
                        .in(MerchantChannelEntity::getMerchantId, childList.stream().map(MerchantEntity::getUserId).collect(Collectors.toList()))
                        .eq(MerchantChannelEntity::getChannelId, channelId)
        );
        owerChannelList.forEach(data -> {
            data.setAgentContrl(agentContrl);
        });
        updateBatchById(owerChannelList);
    }
}
