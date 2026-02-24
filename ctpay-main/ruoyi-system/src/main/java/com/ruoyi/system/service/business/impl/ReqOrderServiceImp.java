package com.ruoyi.system.service.business.impl;

import com.ruoyi.system.domain.business.ReqOrderEntity;
import com.ruoyi.system.domain.dto.ShopOrderReq;
import com.ruoyi.system.mapper.business.ReqOrderMapper;
import com.ruoyi.system.service.business.ReqOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 * 订单请求 服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-11-18
 */
@Service
public class ReqOrderServiceImp extends ServiceImpl<ReqOrderMapper, ReqOrderEntity> implements ReqOrderService {

    @Override
    @Async
    public void addReqOrder(ShopOrderReq reqOrder) {
        try {
            ReqOrderEntity reqOrderEntity = ReqOrderEntity.builder()
                    .mchid(reqOrder.getMchid())
                    .outTradeNo(reqOrder.getOut_trade_no())
                    .amount(reqOrder.getAmount())
                    .channel(reqOrder.getChannel())
                    .notifyUrl(reqOrder.getNotify_url())
                    .returnUrl(reqOrder.getReturn_url())
                    .timeStamp(reqOrder.getTime_stamp())
                    .body(reqOrder.getBody())
                    .sign(reqOrder.getSign())
                    .createTime(new Date())
                    .build();
            baseMapper.insert(reqOrderEntity);
        }catch (Exception e){
            log.error("保存请求订单失败");
        }
    }
}
