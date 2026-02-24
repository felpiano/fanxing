package com.ruoyi.system.service.business.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.enums.OrderStatus;
import com.ruoyi.common.utils.DESUtil;
import com.ruoyi.system.domain.business.InOrderDetailEntity;
import com.ruoyi.system.domain.business.InOrderEntity;
import com.ruoyi.system.domain.business.ShopEntity;
import com.ruoyi.system.mapper.business.InOrderDetailMapper;
import com.ruoyi.system.mapper.business.InOrderMapper;
import com.ruoyi.system.service.business.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CallbackService {

    @Resource
    private ShopService shopService;
    @Resource
    private InOrderMapper inOrderMapper;
    @Resource
    private InOrderDetailMapper inOrderDetailMapper;

    @Async
    public void callBackShop(InOrderEntity order, InOrderDetailEntity detailEntity) {
        Integer callbackNum = 1;
        StringBuilder cr = new StringBuilder();
        ShopEntity shop = shopService.getById(detailEntity.getShopId());
        String sucs = callBackShopBody(order, detailEntity, shop.getSignSecret());
        cr.append("第").append(callbackNum).append("回调，返回参数：").append(sucs);
        if (!StrUtil.equals("SUCCESS", sucs)) {
            for (int i = 1;i <= 5;i++) {
                try {
                    Thread.sleep(10 * 60);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                callbackNum ++;
                log.info("回调失败，重新回调次数：{}，系统订单号：{}", i, order.getTradeNo());
                sucs = callBackShopBody(order, detailEntity, shop.getSignSecret());
                cr.append(";").append("第").append(callbackNum).append("回调，返回参数：").append(sucs);
                if (StrUtil.equals("SUCCESS", sucs)) {
                    break;
                }
            }
        }
        if (StrUtil.equals("SUCCESS", sucs)) {
            order.setCallbackStatus(1);
        } else {
            order.setCallbackStatus(2);
        }
        inOrderMapper.updateById(order);
        detailEntity.setCallbackRemarks(cr.toString());
        inOrderDetailMapper.updateById(detailEntity);
    }

    public String callBackShopBody(InOrderEntity order, InOrderDetailEntity detailEntity, String signSecret) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("amount", order.getOrderAmount());
            map.put("body", order.getFinishTime().getTime());
            map.put("channel", detailEntity.getChannelCode());
            map.put("trade_no", order.getTradeNo());
            map.put("out_trade_no", order.getShopOrderNo());
            map.put("order_status", order.getOrderStatus()== OrderStatus.FINISH.getValue() ?1:0);
            //签名
            String signStr = DESUtil.getSingByMap(map) + "&key=" + signSecret;
            String signNew = DigestUtil.md5Hex(signStr);
            map.put("sign", signNew.toLowerCase());
            log.info("回调商户:{}", JSONUtil.toJsonStr(map));
            HttpRequest request = HttpUtil.createPost(detailEntity.getCallShopUrl());
            //连接超时5秒钟
            request.setConnectionTimeout(5000);
            request.form(map);
            HttpResponse httpResponse = request.execute();
            String result = httpResponse.body();
            log.info("回调商户返回参数：{}", result);
            if (StrUtil.equals("SUCCESS", result)) {
                return "SUCCESS";
            }
        }catch (Exception e) {
            log.info("回调商户异常{}",e.getMessage()+"--"+order.getShopOrderNo()+"--"+order.getTradeNo());
        }
        return "error";
    }
}
