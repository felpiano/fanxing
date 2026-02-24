package com.ruoyi.system.reidsListen;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.enums.OrderStatus;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.RedisLock;
import com.ruoyi.system.domain.business.InOrderDetailEntity;
import com.ruoyi.system.domain.business.InOrderEntity;
import com.ruoyi.system.domain.dto.AmountChangeDTO;
import com.ruoyi.system.service.business.InOrderDetailService;
import com.ruoyi.system.service.business.InOrderService;
import com.ruoyi.system.service.business.MerchantService;
import com.ruoyi.system.service.business.impl.OrderNotPayReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

/**
 * redis失效key监听
 *
 * @author 郑文
 *
 */
@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
    @Resource
    private RedisLock redisLockUtils;
    @Resource
    private MerchantService merchantService;
    @Resource
    private InOrderService inOrderService;
    @Resource
    private InOrderDetailService detailService;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private OrderNotPayReminderService orderNotPayReminderService;


 public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
    super(listenerContainer);
  }

  /**
   * redis失效key事件处理
   * @param message
   * @param pattern
   */
  @Override
  public void onMessage(Message message, byte[] pattern) {
    String expiredKey = message.toString();
    log.info("RedisKeyExpirationListener expiredKey:{}", expiredKey);
     /** 处理超时订单 **/
//      if(expiredKey.startsWith(RedisKeys.CPAY_ORDER_TIMEOUT) || expiredKey.startsWith(RedisKeys.ORDER_BACK_WAIT)) {
      if(expiredKey.startsWith(RedisKeys.CPAY_ORDER_TIMEOUT)) {
          String arr[] = expiredKey.split(":");
          String orderId = arr[arr.length - 1];
          String lockOrderId = RedisKeys.CPAY_ORDER_OP_LOCK+orderId;
          if(redisLockUtils.getLock(lockOrderId,"1")){
              try {
                  InOrderEntity order = inOrderService.getOne(Wrappers.lambdaQuery(InOrderEntity.class)
                          .eq(InOrderEntity::getTradeNo, orderId)
//                                  .eq(InOrderEntity::getOrderStatus, OrderStatus.WAIT.getValue())
                                  .and(a -> a.eq(InOrderEntity::getOrderStatus, OrderStatus.BACKING.getValue())
                                          .or().eq(InOrderEntity::getOrderStatus, OrderStatus.WAIT.getValue()))
                          );
                  if(ObjectUtil.isNotNull(order) && (order.getOrderStatus() == OrderStatus.BACKING.getValue() || order.getOrderStatus() == OrderStatus.WAIT.getValue())) {
//                  if(ObjectUtil.isNotNull(order) &&  order.getOrderStatus() == OrderStatus.WAIT.getValue()) {
                      InOrderDetailEntity detail = detailService.getById(order.getId());
                      //**待支付的订单才会进入**//*
                      //**扣减之后才会归还**//*
                      if (order.getOpcoin() == 1) {
                          //返还码商预付金额
                          BigDecimal changeAmount = order.getOrderAmount();
                          merchantService.updateAmount(AmountChangeDTO.builder()
                                  .userId(order.getMerchantId())
                                  .userName(order.getMerchantName())
                                  .changeAmount(changeAmount)
                                  .amountType(1)
                                  .changeType(1)
                                  .remarks("订单" + order.getTradeNo() + "支付超时，返还余额")
                                  .orderNo(order.getTradeNo())
                                  .agentId(detail.getAgentId())
                                  .build());
                      }
                      //**设置订单超时*/
                      order.setOpcoin(0);
                      order.setFinishTime(new Date());
                      order.setOrderStatus(OrderStatus.TIMEOUT.getValue());
//                      if (expiredKey.startsWith(RedisKeys.CPAY_ORDER_TIMEOUT)){
//                          order.setOrderStatus(OrderStatus.TIMEOUT.getValue());
//                      } else {
//                          order.setOrderStatus(OrderStatus.BACKED.getValue());
//                      }
                      inOrderService.updateById(order);
                      //释放码
                      redisUtils.delete(RedisKeys.lockedQrcode + order.getQrcodeId());
                      //每日金额和笔数减少
                      inOrderService.qrcodeLimitAmountAndCount(order.getQrcodeId(), order.getOrderAmount(), 1);
                      //连续失败笔数增加
                      orderNotPayReminderService.orderNotPayReminder(order, 0);
                  }
              }catch (Exception e){
                  e.printStackTrace();
              }finally {
                  redisLockUtils.releaseLock(lockOrderId,"1");
              }
          }
      }
  }
}
