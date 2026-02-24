package com.ruoyi.system.service.business.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.business.*;
import com.ruoyi.system.domain.dto.ChannelProductDTO;
import com.ruoyi.system.mapper.business.AgentMapper;
import com.ruoyi.system.mapper.business.ChannelMapper;
import com.ruoyi.system.mapper.business.MerchantMapper;
import com.ruoyi.system.mapper.business.ShopMapper;
import com.ruoyi.system.service.business.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 通道 服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-09-08
 */
@Service
public class ChannelServiceImp extends ServiceImpl<ChannelMapper, ChannelEntity> implements ChannelService {
    @Resource
    private ShopBaseChannelService sbcService;
    @Resource
    private ShopMapper shopMapper;
    @Resource
    private MerchantMapper merchantMapper;
    @Resource
    private MerchantChannelService merchantChannelService;
    @Resource
    private AgentMapper agentMapper;
    @Resource
    private AgentChannelService agentChannelService;
    @Resource
    private BaseChannelService baseChannelService;
    @Resource
    private AsyncRedisService asyncRedisService;


    @Override
    public AjaxResult addChannel(ChannelEntity channelEntity) {
        List<ChannelEntity> list = this.list(Wrappers.lambdaQuery(ChannelEntity.class)
                .eq(ChannelEntity::getChannelCode, channelEntity.getChannelCode())
                .or(a -> a.eq(ChannelEntity::getChannelName, channelEntity.getChannelName()))
        );
        if (list != null && !list.isEmpty()) {
            return AjaxResult.error("通道编号或通道名称已存在");
        }
        this.save(channelEntity);
        //同步新增基础通道
        BaseChannelEntity baseChannelEntity = BaseChannelEntity.builder().channelId(channelEntity.getId()).build();
        baseChannelEntity.setFirstSet(0);
        baseChannelService.save(baseChannelEntity);
        //同步新增代理通道
        List<AgentEntity> agentList = agentMapper.selectList(Wrappers.<AgentEntity>lambdaQuery(AgentEntity.class).eq(AgentEntity::getDelFlag, 0));
        if (agentList != null && !agentList.isEmpty()) {
            List<AgentChannelEntity> acList = new ArrayList<>();
            agentList.forEach(agent -> {
                AgentChannelEntity agentChannelEntity = AgentChannelEntity.builder()
                        .channelId(channelEntity.getId())
                        .agentId(agent.getUserId())
                        .build();
                acList.add(agentChannelEntity);
            });
            agentChannelService.saveBatch(acList);
        }
        //同步新增商户通道
        List<ShopEntity> shopList = shopMapper.selectList(Wrappers.lambdaQuery(ShopEntity.class).eq(ShopEntity::getDelFlag, 0));
        if (shopList != null && !shopList.isEmpty()){
            List<ShopBaseChannelEntity> sbcList = new ArrayList<>();
            shopList.forEach(shop -> {
                ShopBaseChannelEntity sbc = ShopBaseChannelEntity.builder()
                        .shopId(shop.getUserId())
                        .channelId(channelEntity.getId())
                        .channelRate(BigDecimal.valueOf(0))
                        .status(0)
                        .build();
                sbcList.add(sbc);
            });
            sbcService.saveBatch(sbcList);
        }
        //同步新增码商通道
        List<MerchantEntity> merchantList = merchantMapper.selectList(Wrappers.lambdaQuery(MerchantEntity.class).eq(MerchantEntity::getDelFlag, 0));
        if (merchantList != null && !merchantList.isEmpty()) {
            List<MerchantChannelEntity> merchantChannelList = new ArrayList<>();
            merchantList.forEach(merchant -> {
                MerchantChannelEntity mc = MerchantChannelEntity.builder()
                        .channelId(channelEntity.getId())
                        .merchantId(merchant.getUserId())
                        .status(0)
                        .minAmount(BigDecimal.valueOf(0))
                        .maxAmount(BigDecimal.valueOf(0))
                        .channelRate(BigDecimal.valueOf(0))
                        .build();
                merchantChannelList.add(mc);
            });
            merchantChannelService.saveBatch(merchantChannelList);
        }
        asyncRedisService.asyncReportQrcode();
        return AjaxResult.success();
    }

    @Override
    @Transactional
    public AjaxResult deleteChannel(Long id) {
        //同步删除基础通道
        baseChannelService.remove(Wrappers.<BaseChannelEntity>lambdaQuery().eq(BaseChannelEntity::getId, id));
        //同时删除商户基础通道
        sbcService.remove(Wrappers.lambdaQuery(ShopBaseChannelEntity.class).eq(ShopBaseChannelEntity::getChannelId, id));
        //同步删除码商通道
        merchantChannelService.remove(Wrappers.lambdaQuery(MerchantChannelEntity.class).eq(MerchantChannelEntity::getChannelId, id));
        //同步删除代理通道
        agentChannelService.remove(Wrappers.lambdaQuery(AgentChannelEntity.class).eq(AgentChannelEntity::getChannelId, id));
        baseMapper.deleteById(id);
        asyncRedisService.asyncReportQrcode();
        return  AjaxResult.success();
    }

    @Override
    @Transactional
    public AjaxResult updateChannelStatus(Long id, Integer status) {
        ChannelEntity agentEntity = ChannelEntity.builder().status(status).id(id).build();
        baseMapper.updateById(agentEntity);
        asyncRedisService.asyncReportQrcode();
        return AjaxResult.success();
    }

    @Override
    public AjaxResult addChannelProduct(ChannelProductDTO dto) {
        ChannelEntity channelEntity = BeanUtil.copyProperties(dto, ChannelEntity.class);
        List<ChannelEntity> list = this.list(Wrappers.lambdaQuery(ChannelEntity.class)
                .eq(ChannelEntity::getChannelCode, channelEntity.getChannelCode())
                .or(a -> a.eq(ChannelEntity::getChannelName, channelEntity.getChannelName()))
        );
        if (list != null && !list.isEmpty()) {
            return AjaxResult.error("通道编号或通道名称已存在");
        }
        this.save(channelEntity);
        //同步新增基础通道
        BaseChannelEntity baseChannelEntity = BeanUtil.copyProperties(dto, BaseChannelEntity.class);
        baseChannelEntity.setFirstSet(0);
        baseChannelService.save(baseChannelEntity);
        //同步新增代理通道
        List<AgentEntity> agentList = agentMapper.selectList(Wrappers.<AgentEntity>lambdaQuery(AgentEntity.class).eq(AgentEntity::getDelFlag, 0));
        if (agentList != null && !agentList.isEmpty()) {
            List<AgentChannelEntity> acList = new ArrayList<>();
            agentList.forEach(agent -> {
                AgentChannelEntity agentChannelEntity = BeanUtil.copyProperties(baseChannelEntity, AgentChannelEntity.class);
                agentChannelEntity.setId(null);
                acList.add(agentChannelEntity);
            });
            agentChannelService.saveBatch(acList);
        }
        //同步新增商户通道
        List<ShopEntity> shopList = shopMapper.selectList(Wrappers.lambdaQuery(ShopEntity.class).eq(ShopEntity::getDelFlag, 0));
        if (shopList != null && !shopList.isEmpty()){
            List<ShopBaseChannelEntity> sbcList = new ArrayList<>();
            shopList.forEach(shop -> {
                ShopBaseChannelEntity sbc = ShopBaseChannelEntity.builder()
                        .shopId(shop.getUserId())
                        .channelId(channelEntity.getId())
                        .channelRate(BigDecimal.valueOf(0))
                        .status(0)
                        .build();
                sbcList.add(sbc);
            });
            sbcService.saveBatch(sbcList);
        }
        //同步新增码商通道
        List<MerchantEntity> merchantList = merchantMapper.selectList(Wrappers.lambdaQuery(MerchantEntity.class).eq(MerchantEntity::getDelFlag, 0));
        if (merchantList != null && !merchantList.isEmpty()) {
            List<MerchantChannelEntity> merchantChannelList = new ArrayList<>();
            merchantList.forEach(merchant -> {
                MerchantChannelEntity mc = MerchantChannelEntity.builder()
                        .channelId(channelEntity.getId())
                        .merchantId(merchant.getUserId())
                        .status(0)
                        .minAmount(BigDecimal.valueOf(0))
                        .maxAmount(BigDecimal.valueOf(0))
                        .channelRate(BigDecimal.valueOf(0))
                        .build();
                merchantChannelList.add(mc);
            });
            merchantChannelService.saveBatch(merchantChannelList);
        }
        asyncRedisService.asyncReportQrcode();
        return AjaxResult.success();
    }

    @Override
    public AjaxResult updateBaseChannel(BaseChannelEntity baseChannelEntity) {
        BaseChannelEntity baseChannel = baseChannelService.getById(baseChannelEntity.getId());
        if (baseChannel != null && baseChannel.getFirstSet() == 0) {
            List<AgentChannelEntity> list = agentChannelService.list(Wrappers.lambdaQuery(AgentChannelEntity.class)
                    .eq(AgentChannelEntity::getChannelId, baseChannelEntity.getChannelId()));
            if (list != null && !list.isEmpty()) {
                list.forEach(data -> {
                    data.setCostRate(baseChannelEntity.getCostRate());
                    data.setOvertime(baseChannelEntity.getOvertime());
                    data.setOverAmount(baseChannelEntity.getOverAmount());
                    data.setPayurlUser(baseChannelEntity.getPayurlUser());
                    data.setInitpageForwrod(baseChannelEntity.getInitpageForwrod());
                    data.setQrcodeModel(baseChannelEntity.getQrcodeModel());
                    data.setOverAmountValue(baseChannelEntity.getOverAmountValue());
                    data.setOverAmountDown(baseChannelEntity.getOverAmountDown());
                    data.setGuidance(baseChannelEntity.getGuidance());
                    data.setSetAmount(baseChannelEntity.getSetAmount());
                    data.setLockedCode(baseChannelEntity.getLockedCode());
                    data.setHideQrcode(baseChannelEntity.getHideQrcode());
                    data.setMaxCode(baseChannelEntity.getMaxCode());
                    data.setShowChannel(baseChannelEntity.getShowChannel());
                    data.setShowConfirm(baseChannelEntity.getShowConfirm());
                });
                agentChannelService.updateBatchById(list);
            }
        }
        baseChannelEntity.setFirstSet(1);
        baseChannelService.updateById(baseChannelEntity);
        asyncRedisService.asyncReportQrcode();
        return AjaxResult.success();
    }
}
