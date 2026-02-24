package com.ruoyi.system.service.business.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.enums.OrderStatus;
import com.ruoyi.common.utils.*;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.system.domain.business.*;
import com.ruoyi.system.domain.dto.AmountChangeDTO;
import com.ruoyi.system.domain.dto.OrderQueryDTO;
import com.ruoyi.system.domain.vo.MerchantOrderTotalVO;
import com.ruoyi.system.domain.vo.SuitableQrcodeVO;
import com.ruoyi.system.domain.vo.SytQueryVO;
import com.ruoyi.system.mapper.business.InOrderMapper;
import com.ruoyi.system.service.business.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.system.telegram.SFTelegramBot;
import com.ruoyi.system.telegram.tgDataEntity.TgOrderInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-10-20
 */
@Service
@Slf4j(topic = "ct-business")
public class InOrderServiceImp extends ServiceImpl<InOrderMapper, InOrderEntity> implements InOrderService {

    @Resource
    private MerchantService merchantService;
    @Resource
    private AgentService agentService;
    @Autowired
    private RedisUtils redisUtils;
    @Resource
    private RedisLock redisLock;
    @Resource
    private ShopService shopService;
    @Resource
    private InOrderDetailService detailService;
    @Resource
    private CallbackService callbackService;
    @Resource
    private OrderNotPayReminderService orderNotPayReminderService;
    @Resource
    private SFTelegramBot sfTelegramBot;

    @Override
    public List<SuitableQrcodeVO> taskByCreateOrder() {
        return baseMapper.taskByCreateOrder();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrder(InOrderEntity order, SuitableQrcodeVO lastQrcode) {
        Long uid;
        try {
            uid = redisUtils.incr(RedisKeys.merchantUidLock + order.getQrcodeId(), 1);
        } catch (Exception e) {
            uid = new Random().nextInt(90) + 10L;
        }
        String uuid = StrUtil.toString(uid);
        uuid = uuid.length() > 4 ? uuid.substring(uuid.length() - 4) : uuid;
        String uidStr = StrUtil.fillBefore(uuid, '0', 4);
        uidStr = new Random().nextInt(90) + 10 + uidStr;
        //订单唯一码
        order.setUnionCode(Integer.parseInt(uidStr));
        //超时时长
        Long overtime = lastQrcode.getMerchantOvertime()==0?lastQrcode.getOvertime():lastQrcode.getMerchantOvertime();
        order.setOvertime(overtime.intValue());
        //订单详情
        InOrderDetailEntity detailEntity = InOrderDetailEntity.builder()
                .agentId(lastQrcode.getAgentId())
                .agentName(lastQrcode.getAgentName())
                .shopId(lastQrcode.getShopId())
                .shopName(lastQrcode.getShopName())
                .merchantRate(lastQrcode.getMerchantRate())
                .merchantFee(order.getOrderAmount().multiply(lastQrcode.getMerchantRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .shopRate(lastQrcode.getShopRate())
                .shopFee(order.getOrderAmount().multiply(lastQrcode.getShopRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .agentRate(lastQrcode.getAgentRate())
                .agentFee(order.getOrderAmount().multiply(lastQrcode.getAgentRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .channelCode(lastQrcode.getChannelCode())
                .callShopUrl(lastQrcode.getNotify_url())
                .merchantChannelId(lastQrcode.getMerchantChannelId())
                .agentChannelId(lastQrcode.getAgentChannelId())
                .forwordUrl(lastQrcode.getForwordUrl())
                .merchantRateOne(lastQrcode.getMerchantRateOne())
                .merchantRateTwo(lastQrcode.getMerchantRateTwo())
                .merchantRateThree(lastQrcode.getMerchantRateThree())
                .merchantRateFour(lastQrcode.getMerchantRateFour())
                .merchantRateFive(lastQrcode.getMerchantRateFive())
                .build();
        //扣减码商预付金额
        BigDecimal changeAmount = order.getOrderAmount().multiply(new BigDecimal(-1));
        merchantService.subAmount(AmountChangeDTO.builder()
                        .userId(order.getMerchantId())
                        .userName(order.getMerchantName())
                        .changeAmount(changeAmount)
                        .amountType(1)
                        .changeType(1)
                        .remarks("订单" + order.getTradeNo() + "下单成功，扣减预付金额,订单金额：" + order.getOrderAmount())
                        .orderNo(order.getTradeNo())
                        .agentId(detailEntity.getAgentId())
                .build());

        //创建监控订单
        redisUtils.set(RedisKeys.CPAY_ORDER_TIMEOUT + order.getTradeNo(), "", overtime * 60);
        //锁码
        if (lastQrcode.getLockedCode() == 0) {
            redisUtils.set(RedisKeys.lockedQrcode + order.getQrcodeId(), "1", overtime * 60);
        }
        //每日金额和笔数增加
        qrcodeLimitAmountAndCount(order.getQrcodeId(), order.getOrderAmount(), 0);
        //创建订单
        order.setOpcoin(1);
        baseMapper.insert(order);
        detailEntity.setOrderId(order.getId());
        detailService.save(detailEntity);
        //将来单信息插入队列
        if (lastQrcode.getOrderRemind() == 0) {
            try {
                JSONObject json = new JSONObject();
                json.set("userId", order.getMerchantId());
                json.set("message", order.getTradeNo());
                redisUtils.setRange(RedisKeys.orderRemind, JSONUtil.toJsonStr(json));
            } catch (Exception ignored) {
                log.error("来单提醒插入队列失败");
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult repair(InOrderEntity order,InOrderDetailEntity detailEntity) {
        String lockOrderId = RedisKeys.CPAY_ORDER_OP_LOCK+order.getTradeNo();
        try {
            if (redisLock.getLock(lockOrderId,"1")) {
                order = this.getById(order.getId());
                detailEntity = detailService.getById(order.getId());
                if (order.getOrderStatus() == OrderStatus.FINISH.getValue()) {
                    return AjaxResult.error("订单已是成功状态");
                }
                order.setOrderStatus(OrderStatus.FINISH.getValue());
                order.setFinishTime(new Date());
                detailEntity.setOrderRemark(SecurityUtils.getUsername() + "确认收款，系统订单号：" + order.getTradeNo());
                //为商户增加余额
                BigDecimal shopChange = order.getOrderAmount();
                if (shopChange.compareTo(BigDecimal.ZERO) != 0) {
                    shopService.updateShopAmount(AmountChangeDTO.builder()
                            .userId(detailEntity.getShopId())
                            .userName(detailEntity.getShopName())
                            .changeAmount(shopChange)
                            .agentId(detailEntity.getAgentId())
                            .amountType(1)
                            .changeType(1)
                            .agentId(detailEntity.getAgentId())
                            .remarks("订单" + order.getTradeNo() + "支付完成，余额增加，订单金额:" + order.getOrderAmount())
                            .orderNo(order.getTradeNo())
                            .build());
                }
                //为代理商扣减余额
                BigDecimal agentChange = detailEntity.getAgentFee().multiply(new BigDecimal(-1));
                if (agentChange.compareTo(BigDecimal.ZERO) != 0) {
                    agentService.updateAgentBalance(AmountChangeDTO.builder()
                            .userId(detailEntity.getAgentId())
                            .userName(detailEntity.getAgentName())
                            .changeAmount(agentChange)
                            .amountType(1)
                            .changeType(1)
                            .remarks("订单" + order.getTradeNo() + "支付完成，余额扣减，订单金额:" + order.getOrderAmount() + "，手续费：" + detailEntity.getAgentFee())
                            .orderNo(order.getTradeNo())
                            .build());
                }
                //如果订单的扣减余额标识为0，则扣减码商余额
                if (order.getOpcoin() == 0) {
                    order.setOpcoin(1);
                    BigDecimal changeAmount = order.getOrderAmount().multiply(new BigDecimal(-1)) ;
                    merchantService.updateAmount(AmountChangeDTO.builder()
                            .userId(order.getMerchantId())
                            .userName(order.getMerchantName())
                            .changeAmount(changeAmount)
                            .amountType(1)
                            .changeType(1)
                            .remarks("订单" + order.getTradeNo() + "支付完成，订单金额：" + order.getOrderAmount())
                            .orderNo(order.getTradeNo())
                            .agentId(detailEntity.getAgentId())
                            .build());
                    //每日金额和笔数增加
                    qrcodeLimitAmountAndCount(order.getQrcodeId(), order.getOrderAmount(), 0);
                }
                order.setOperater(SecurityUtils.getUsername());
                order.setOperaterTime(new Date());
                baseMapper.updateById(order);
                detailService.updateById(detailEntity);
                //释放码
                redisUtils.delete(RedisKeys.lockedQrcode + order.getQrcodeId());
                //佣金
                merchantService.addMerchantCommission(order, detailEntity);
                //回调商户
                callbackService.callBackShop(order, detailEntity);
                //连续失败笔数清除
                orderNotPayReminderService.orderNotPayReminder(order, 1);
                return AjaxResult.success();
            } else {
                return AjaxResult.error("该订单被锁定，请稍后重试");
            }
        }catch (Exception e){
            log.error("补单异常-{}", e.getMessage());
        }finally {
            redisLock.releaseLock(lockOrderId, "1");
        }
        return AjaxResult.error("确认失败，请稍后重试");
    }

    @Override
    public AjaxResult autoRepair(InOrderEntity order, InOrderDetailEntity detailEntity, String operatorMsg) {
        String lockOrderId = RedisKeys.CPAY_ORDER_OP_LOCK+order.getTradeNo();
        try {
            if (redisLock.getLock(lockOrderId,"1")) {
                order = this.getById(order.getId());
                detailEntity = detailService.getById(order.getId());
                if (order.getOrderStatus() == OrderStatus.FINISH.getValue()) {
                    return AjaxResult.error("订单已是成功状态");
                }
                order.setOrderStatus(OrderStatus.FINISH.getValue());
                order.setFinishTime(new Date());
                detailEntity.setOrderRemark(operatorMsg + "确认收款，系统订单号：" + order.getTradeNo());
                //为商户增加余额
                BigDecimal shopChange = order.getOrderAmount();
                if (shopChange.compareTo(BigDecimal.ZERO) != 0) {
                    shopService.updateShopAmount(AmountChangeDTO.builder()
                            .userId(detailEntity.getShopId())
                            .userName(detailEntity.getShopName())
                            .changeAmount(shopChange)
                            .agentId(detailEntity.getAgentId())
                            .amountType(1)
                            .changeType(1)
                            .agentId(detailEntity.getAgentId())
                            .remarks("订单" + order.getTradeNo() + "支付完成，余额增加，订单金额:" + order.getOrderAmount())
                            .orderNo(order.getTradeNo())
                            .build());
                }
                //为代理商扣减余额
                BigDecimal agentChange = detailEntity.getAgentFee().multiply(new BigDecimal(-1));
                if (agentChange.compareTo(BigDecimal.ZERO) != 0) {
                    agentService.updateAgentBalance(AmountChangeDTO.builder()
                            .userId(detailEntity.getAgentId())
                            .userName(detailEntity.getAgentName())
                            .changeAmount(agentChange)
                            .amountType(1)
                            .changeType(1)
                            .remarks("订单" + order.getTradeNo() + "支付完成，余额扣减，订单金额:" + order.getOrderAmount() + "，手续费：" + detailEntity.getAgentFee())
                            .orderNo(order.getTradeNo())
                            .build());
                }
                //如果订单的扣减余额标识为0，则扣减码商余额
                if (order.getOpcoin() == 0) {
                    order.setOpcoin(1);
                    BigDecimal changeAmount = order.getOrderAmount().multiply(new BigDecimal(-1)) ;
                    merchantService.updateAmount(AmountChangeDTO.builder()
                            .userId(order.getMerchantId())
                            .userName(order.getMerchantName())
                            .changeAmount(changeAmount)
                            .amountType(1)
                            .changeType(1)
                            .remarks("订单" + order.getTradeNo() + "支付完成，订单金额：" + order.getOrderAmount())
                            .orderNo(order.getTradeNo())
                            .agentId(detailEntity.getAgentId())
                            .build());
                    //每日金额和笔数增加
                    qrcodeLimitAmountAndCount(order.getQrcodeId(), order.getOrderAmount(), 0);
                }
                order.setOperater(operatorMsg);
                order.setOperaterTime(new Date());
                baseMapper.updateById(order);
                detailService.updateById(detailEntity);
                //释放码
                redisUtils.delete(RedisKeys.lockedQrcode + order.getQrcodeId());
                //佣金
                merchantService.addMerchantCommission(order, detailEntity);
                //回调商户
                callbackService.callBackShop(order, detailEntity);
                //连续失败笔数清除
                orderNotPayReminderService.orderNotPayReminder(order, 1);
                return AjaxResult.success();
            } else {
                return AjaxResult.error("该订单被锁定，请稍后重试");
            }
        }catch (Exception e){
            log.error("补单异常-{}", e.getMessage());
        }finally {
            redisLock.releaseLock(lockOrderId, "1");
        }
        return AjaxResult.error("确认失败，请稍后重试");
    }

    @Override
    public AjaxResult unPaid(InOrderEntity order, InOrderDetailEntity detailEntity) {
        order.setOrderStatus(OrderStatus.BACKING.getValue());
        order.setFinishTime(new Date());
        this.updateById(order);
//        redisUtils.set(RedisKeys.ORDER_BACK_WAIT + order.getTradeNo(), "", 5 * 60);
        return AjaxResult.success();
    }

    @Override
    public R<SytQueryVO> getSytByTradeNo(String tradeNo) {
        InOrderEntity order = this.getOne(Wrappers.lambdaQuery(InOrderEntity.class).eq(
                InOrderEntity::getTradeNo, tradeNo
        ));
        if (ObjectUtil.isNotEmpty(order)) {
            InOrderDetailEntity detailEntity = detailService.getById(order.getId());
            List<AgentChannelEntity> agentChannelList = JSONUtil.toList( redisUtils.get(RedisKeys.agentChannel), AgentChannelEntity.class);
            AgentChannelEntity agentChannel = agentChannelList.stream().filter(a -> a.getId().equals(detailEntity.getAgentChannelId())
                && a.getAgentId().equals(detailEntity.getAgentId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(agentChannel) && ObjectUtil.isNull(agentChannel.getJumpType())) {
                agentChannel.setJumpType(0);
            }
            ShopEntity shop = shopService.getById(detailEntity.getShopId());
            MerchantQrcodeEntity qrcode = BeanUtil.copyProperties(order, MerchantQrcodeEntity.class);
            SytQueryVO vo = SytQueryVO.builder()
                    .orderStatus(order.getOrderStatus())
                    .agentChannel(agentChannel)
                    .merchantQrcode(qrcode)
                    .orderTime(order.getOrderTime())
                    .overtime(order.getOvertime())
                    .tradeNo(order.getTradeNo())
                    .fixedAmount(order.getFixedAmount())
                    .uid(order.getUnionCode())
                    .channelName(order.getChannelName())
                    .memberNameFlag(shop != null?shop.getMemberNameFlag():0)
                    .build();
            try{
                InOrderEntity updateOrder = InOrderEntity.builder()
                        .id(order.getId())
                        .memberIp(IpUtils.getIpAddr())
                        .build();
                this.updateById(updateOrder);
            }catch (Exception e){
                log.error("保存会员IP失败");
            }
            return R.ok(vo);
        }
        return R.fail("订单不存在");
    }

    @Override
    public AjaxResult setOrderPayer(String tradeNo, String payer) {
        InOrderEntity order = this.getOne(Wrappers.lambdaQuery(InOrderEntity.class).eq(
                InOrderEntity::getTradeNo, tradeNo
        ));
        log.info("收银台用户信息：{},对应订单{}", tradeNo, JSONUtil.toJsonStr(order));
        if (ObjectUtil.isEmpty(order) || (order.getOrderStatus() != OrderStatus.WAIT.getValue() && order.getOrderStatus() != OrderStatus.BACKING.getValue())) {
            log.error("收银台请求信息错误：{}", tradeNo);
            return AjaxResult.error("订单已超时，请重新发起订单");
        }
        if (ObjectUtil.isNotEmpty(order)) {
            order.setPayer(payer);
            order.setClientIp(IpUtils.getIpAddr());
            this.updateById(order);
            return AjaxResult.success();
        }
        return AjaxResult.error("订单不存在");
    }


    @Override
    public MerchantOrderTotalVO merchantOrderTotal(OrderQueryDTO dto) {
        MerchantOrderTotalVO vo = baseMapper.merchantOrderTotal(dto);
        if (vo != null) {
            if (ObjectUtil.isNotEmpty(vo.getTotalCount()) && vo.getTotalCount() != 0) {
                vo.setSuccessRate(new BigDecimal(vo.getSuccessCount()).divide(new BigDecimal(vo.getTotalCount()), 2, RoundingMode.HALF_UP));
            } else {
                vo.setSuccessRate(new BigDecimal(0));
            }
        }
        return vo;
    }

    @Override
    public void qrcodeLimitAmountAndCount(Long qrcodeId, BigDecimal amount, Integer type) {
        String amountKeys = RedisKeys.merchantQrcodeLimitAmount + qrcodeId;
        String countKeys = RedisKeys.merchantQrcodeLimitCount + qrcodeId;
        if (type == 0) {
            redisUtils.addMonery(amountKeys, amount.toString());
            redisUtils.addMonery(countKeys, "1");
        } else {
            redisUtils.addMonery(amountKeys, amount.multiply(new BigDecimal(-1)).toString());
            redisUtils.addMonery(countKeys, "-1");
        }
    }

    @Override
    public TgOrderInfo queryTgOrder(TgOrderInfo dto){
        return baseMapper.queryTgOrder(dto);
    }

    @Override
    public AjaxResult czOrder(Long orderId) {
        InOrderEntity order = this.getById(orderId);
        String lockOrderId = RedisKeys.CPAY_ORDER_OP_LOCK+order.getTradeNo();
        try {
            if (redisLock.getLock(lockOrderId, "1")) {
                order = this.getById(orderId);
                if (order == null) {
                    return AjaxResult.error("订单已被删除");
                }
                InOrderDetailEntity detailEntity = detailService.getById(order.getId());
                if (detailEntity == null) {
                    return AjaxResult.error("订单已被删除");
                }
                if (order.getOrderStatus() != 1) {
                    return AjaxResult.error("订单未支付完成，无法冲正");
                }
                if (order.getOpcoin() == 1) {
                  //增加码商余额，扣减商户余额
                    BigDecimal shopChange = order.getOrderAmount().multiply(new BigDecimal("-1"));
                    if (shopChange.compareTo(BigDecimal.ZERO) != 0) {
                        shopService.updateShopAmount(AmountChangeDTO.builder()
                                .userId(detailEntity.getShopId())
                                .userName(detailEntity.getShopName())
                                .changeAmount(shopChange)
                                .agentId(detailEntity.getAgentId())
                                .amountType(1)
                                .changeType(4)
                                .agentId(detailEntity.getAgentId())
                                .remarks("订单冲正")
                                .orderNo(order.getTradeNo())
                                .build());
                    }
                    //为代理商增加余额
                    BigDecimal agentChange = detailEntity.getAgentFee();
                    if (agentChange.compareTo(BigDecimal.ZERO) != 0) {
                        agentService.updateAgentBalance(AmountChangeDTO.builder()
                                .userId(detailEntity.getAgentId())
                                .userName(detailEntity.getAgentName())
                                .changeAmount(agentChange)
                                .amountType(1)
                                .changeType(4)
                                .remarks("订单冲正，余额增加")
                                .orderNo(order.getTradeNo())
                                .build());
                    }
                    //增加码商余额
                    BigDecimal changeAmount = order.getOrderAmount();
                    merchantService.updateAmount(AmountChangeDTO.builder()
                            .userId(order.getMerchantId())
                            .userName(order.getMerchantName())
                            .changeAmount(changeAmount)
                            .amountType(1)
                            .changeType(4)
                            .remarks("订单冲正")
                            .orderNo(order.getTradeNo())
                            .agentId(detailEntity.getAgentId())
                            .build());
                    //每日金额和笔数减少
                    qrcodeLimitAmountAndCount(order.getQrcodeId(), order.getOrderAmount(), 1);
                    //佣金充值
                    merchantService.subMerchantCommission(order.getTradeNo());
                }
                order.setOpcoin(0);
                order.setOrderStatus(3);
                this.updateById(order);
                return AjaxResult.success();
            }
        }catch (Exception e){
            log.error("冲正异常{}", e.getMessage());
        }finally {
            redisLock.releaseLock(lockOrderId, "1");
        }
        return AjaxResult.error("系统繁忙，请稍后再试");
    }

}
