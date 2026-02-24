package com.ruoyi.system.service.business.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.constant.ChannelConstants;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.system.domain.business.InOrderDetailEntity;
import com.ruoyi.system.domain.business.InOrderEntity;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.ruoyi.system.domain.business.MerchantQrcodeEntity;
import com.ruoyi.system.domain.dto.OnebuyCreateOrderReq;
import com.ruoyi.system.domain.dto.OnebuyCreateOrderRes;
import com.ruoyi.system.domain.dto.OnebuyOrderReq;
import com.ruoyi.system.mapper.business.InOrderMapper;
import com.ruoyi.system.service.business.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "ct-business")
public class OnebuyServiceImpl implements OnebuyService {

    @Resource
    private MerchantQrcodeService merchantQrcodeService;
    @Resource
    private InOrderService inOrderService;
    @Resource
    private InOrderDetailService detailService;
    @Resource
    private InOrderMapper inOrderMapper;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private MerchantService merchantService;

    @Override
    public void heartbeat(List<String> shopIdList) {
        if (CollUtil.isEmpty(shopIdList)) {
            LambdaUpdateWrapper<MerchantQrcodeEntity> offlineUpdateWrapper = new LambdaUpdateWrapper<>();
            offlineUpdateWrapper.set(MerchantQrcodeEntity::getStatus, 1)
                    .eq(MerchantQrcodeEntity::getChannelId, ChannelConstants.CHANNEL_ID_ONEBUY);
            merchantQrcodeService.update(offlineUpdateWrapper);
        } else {
            LambdaUpdateWrapper<MerchantQrcodeEntity> onlineUpdateWrapper = new LambdaUpdateWrapper<>();
            onlineUpdateWrapper.set(MerchantQrcodeEntity::getStatus, 0)
                    .eq(MerchantQrcodeEntity::getChannelId, ChannelConstants.CHANNEL_ID_ONEBUY)
                    .in(MerchantQrcodeEntity::getAccountNumber, shopIdList);
            merchantQrcodeService.update(onlineUpdateWrapper);

            LambdaUpdateWrapper<MerchantQrcodeEntity> offlineUpdateWrapper = new LambdaUpdateWrapper<>();
            offlineUpdateWrapper.set(MerchantQrcodeEntity::getStatus, 1)
                    .eq(MerchantQrcodeEntity::getChannelId, ChannelConstants.CHANNEL_ID_ONEBUY)
                    .notIn(MerchantQrcodeEntity::getAccountNumber, shopIdList);
            merchantQrcodeService.update(offlineUpdateWrapper);
        }


    }

    @Override
    public void online(List<String> shopIdList) {
        LambdaUpdateWrapper<MerchantQrcodeEntity> onlineUpdateWrapper = new LambdaUpdateWrapper<>();
        onlineUpdateWrapper.set(MerchantQrcodeEntity::getStatus, 0)
                .eq(MerchantQrcodeEntity::getChannelId, ChannelConstants.CHANNEL_ID_ONEBUY)
                .in(MerchantQrcodeEntity::getAccountNumber, shopIdList);
        merchantQrcodeService.update(onlineUpdateWrapper);
    }

    @Override
    public void offline(List<String> shopIdList) {
        LambdaUpdateWrapper<MerchantQrcodeEntity> offlineUpdateWrapper = new LambdaUpdateWrapper<>();
        offlineUpdateWrapper.set(MerchantQrcodeEntity::getStatus, 1)
                .eq(MerchantQrcodeEntity::getChannelId, ChannelConstants.CHANNEL_ID_ONEBUY)
                .in(MerchantQrcodeEntity::getAccountNumber, shopIdList);
        merchantQrcodeService.update(offlineUpdateWrapper);
    }

    @Override
    public void orderReport(List<OnebuyOrderReq> list) {
        list.forEach(onebuyOrderReq -> {
            try {
                String orderNo = onebuyOrderReq.getOrder_no();
                log.info("开始处理一码通上报：订单号:{},内容：{}",orderNo, JSONUtil.toJsonStr(onebuyOrderReq));
                if ("1".equals(onebuyOrderReq.getStatus())) {
                    InOrderEntity order = inOrderService.getOne(Wrappers.lambdaQuery(InOrderEntity.class).eq(InOrderEntity::getOnebuyOrderNo, orderNo));
                    if (order == null) {
                        log.info("处理一码通上报：订单号:{},订单已被删除",orderNo);
                        return;
                    }
                    BigDecimal reportAmount = new BigDecimal(onebuyOrderReq.getAmount().trim());
                    BigDecimal orderAmount = order.getOrderAmount().multiply(BigDecimal.valueOf(100));
                    if (orderAmount.compareTo(reportAmount) != 0) {
                        log.info("处理一码通上报：订单号:{},订单金额不一致",orderNo);
                        return;
                    }

                    InOrderDetailEntity detailEntity = detailService.getById(order.getId());
                    MerchantEntity merchant = merchantService.getById(order.getMerchantId());

                    //判断余额是否充足,订单已超时，且码商余额不足时，不允许回调
                    if (order.getOpcoin() != 1) {
                        String merchantBalance = redisUtils.get(RedisKeys.merchantBalance + merchant.getUserId());
                        if (StrUtil.isEmpty(merchantBalance) || new BigDecimal(merchantBalance).compareTo(order.getOrderAmount()) < 0) {
                            log.info("处理一码通上报：订单号:{},余额不足，请充值",orderNo);
                        }
                    }
                    inOrderService.autoRepair(order, detailEntity, "一码通订单上报");

                } else {
                    log.info("处理一码通上报订单未支付或支付失败，不处理,订单号:{},",orderNo);
                }
            } catch (Exception e) {
                log.error("处理一码通上报，异常,msg={}", e.getMessage(), e);
            }
        });
    }

    @Override
    public OnebuyCreateOrderRes createOnebuyOrder(String orderNo, String amount, List<String> shopIdList) throws ServiceException {
        String uuid = IdUtil.fastSimpleUUID();
        try {
            LambdaQueryWrapper<MerchantQrcodeEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MerchantQrcodeEntity::getChannelId, ChannelConstants.CHANNEL_ID_ONEBUY)
                    .in(MerchantQrcodeEntity::getAccountNumber, shopIdList)
                    .eq(MerchantQrcodeEntity::getStatus, 0);
            List<MerchantQrcodeEntity> qrcodeList = merchantQrcodeService.list(queryWrapper);
            if (CollUtil.isEmpty(qrcodeList)) {
                throw new ServiceException("一码通下单失败：无可用的码");
            }
            OnebuyCreateOrderReq req = new OnebuyCreateOrderReq();
            req.setOrder_no(orderNo);
            req.setAmount(StrUtil.subBefore(amount, ".", true));
            req.setShop(qrcodeList.stream().map(MerchantQrcodeEntity::getAccountNumber).collect(Collectors.toList()));
            String url = "http://127.0.0.1:15007/YMTAPI/CashierDesk/Checkout";
            log.info("一码通下单：uuid:{},url:{}", uuid, url);
            log.info("一码通下单：uuid:{},请求参数:{}", uuid, JSONUtil.toJsonStr(req));
            String result = HttpUtil.post(url, JSONUtil.toJsonStr(req));
//        String result = "{ \"code\": 200,\"message\": \"成功\", \"data\": {\"url\": \"http://www.baidu.com\", \"id\":\"123465\",\"shop_id\":\"999988888\" } }";
            log.info("一码通下单：uuid:{},响应数据:{}", uuid, result);
            JSONObject resultObject = JSONUtil.parseObj(result);
            if (!"200".equals(resultObject.getStr("code"))) {
                throw new ServiceException("一码通下单失败：" + resultObject.getStr("message"));
            }
            OnebuyCreateOrderRes onebuyCreateOrderRes = resultObject.getBean("data", OnebuyCreateOrderRes.class);
            if (onebuyCreateOrderRes == null || StrUtil.isBlank(onebuyCreateOrderRes.getShop_id()) || StrUtil.isBlank(onebuyCreateOrderRes.getUrl())) {
                throw new ServiceException("一码通下单失败：未返回支付地址");
            }
            return onebuyCreateOrderRes;
        } catch (Exception e) {
            log.error("一码通下单：uuid:{},系统异常:{}", uuid, e.getMessage(), e);
            if (e instanceof ServiceException) {
                throw new ServiceException(e.getMessage());
            } else {
                throw new ServiceException("一码通下单失败：系统异常");
            }
        }

    }
}
