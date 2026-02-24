package com.ruoyi.system.service.business.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.WeightRandom;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.constant.ChannelConstants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.enums.BusinessConfigKey;
import com.ruoyi.common.enums.OrderStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DESUtil;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.RedisLock;
import com.ruoyi.system.domain.business.*;
import com.ruoyi.system.domain.dto.OnebuyCreateOrderRes;
import com.ruoyi.system.domain.dto.ShopOrderReq;
import com.ruoyi.system.domain.vo.SuitableQrcodeVO;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.business.ICtOnebuyBlackService;
import com.ruoyi.system.service.business.InOrderService;
import com.ruoyi.system.service.business.OnebuyService;
import com.ruoyi.system.service.impl.SnowflakeIdService;
import com.ruoyi.system.telegram.SFTelegramBot;
import com.ruoyi.system.telegram.tgDataEntity.TgOrderInfo;
import com.ruoyi.system.weight.WeightedRandomSelector;
import com.ruoyi.system.weight.WeightedRandomSelectorMerchant;
import com.ruoyi.system.weight.WeightedRandomSelectorNew;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "ct-business")
public class ShopOrderService {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private SnowflakeIdService snowflakeIdService;

    @Resource
    private InOrderService orderService;

    @Resource
    private ISysConfigService sysConfigService;
    @Autowired
    private RedisLock redisLock;
    @Resource
    private SFTelegramBot sfTelegramBot;
    @Autowired
    private OnebuyService onebuyService;
    @Autowired
    private ICtOnebuyBlackService iCtOnebuyBlackService;

    public AjaxResult createOrder(ShopOrderReq req, String clientIp) {
        if (StrUtil.isEmpty(req.getMchid())) {
            throw new ServiceException("商户id为空");
        }
        if (StrUtil.isEmpty(req.getOut_trade_no())) {
            throw new ServiceException("订单号为空");
        }
        if (StrUtil.isEmpty(req.getAmount())) {
            throw new ServiceException("下单金额为空");
        }
        if (StrUtil.isEmpty(req.getChannel())) {
            throw new ServiceException("支付通道编码为空");
        }
        if (StrUtil.isEmpty(req.getNotify_url())) {
            throw new ServiceException("回调通知地址为空");
        }
        if (StrUtil.isEmpty(req.getReturn_url())) {
            throw new ServiceException("同步通知地址为空");
        }
        if (StrUtil.isEmpty(req.getTime_stamp())) {
            throw new ServiceException("时间为空");
        }
        if (StrUtil.isEmpty(req.getBody())) {
            throw new ServiceException("body为空");
        }
        if (StrUtil.isEmpty(req.getSign())) {
            throw new ServiceException("签名为空");
        }
        /*查找合适的码*/
        if (!redisUtils.hasKey(RedisKeys.suitableQrcodeList)) {
            throw new ServiceException("没有可进单的二维码");
//            return AjaxResult.error(1,"没有可用的二维码");
        }
        String json = redisUtils.get(RedisKeys.suitableQrcodeList);
        if (StrUtil.isEmpty(json)) {
            throw new ServiceException("没有可进单的二维码");
        }
        List<SuitableQrcodeVO> qrCodeList = JSONUtil.toList(json, SuitableQrcodeVO.class);
        if (qrCodeList == null || qrCodeList.isEmpty()) {
            throw new ServiceException("没有可进单的二维码");
        } else {
            qrCodeList = qrCodeList.stream().distinct().collect(Collectors.toList());
        }

        //根据商户ID匹配
        List<SuitableQrcodeVO> shopMatchList = qrCodeList.stream().filter(code ->
                StrUtil.equals(code.getShopId().toString(), req.getMchid())
        ).collect(Collectors.toList());
        if (shopMatchList.isEmpty()) {
            log.error("商户ID错误，{}", JSONUtil.toJsonStr(req));
            throw new ServiceException("该商户没有可进单的二维码");
        }
        /*验证签名*/
        String shopSignSecret = shopMatchList.get(0).getSignSecret();
        Map<String, Object> mapreq = BeanUtil.beanToMap(req);
        mapreq.remove("sign");
        String signStr = DESUtil.getSingByMap(mapreq) + "&key=" + shopSignSecret;
        String signNew = DigestUtil.md5Hex(signStr);
        if (!StrUtil.equals(signNew.toLowerCase(), req.getSign())) {
            log.error("签名有误，{}", JSONUtil.toJsonStr(req));
            throw new ServiceException("签名有误");
        }

        //根据通道编号匹配
        List<SuitableQrcodeVO> match1 = shopMatchList.stream().filter(code ->
                ObjectUtil.equals(code.getChannelCode(), req.getChannel())
        ).collect(Collectors.toList());
        if (match1.isEmpty()) {
            log.error("支付通道编码错误，{}", JSONUtil.toJsonStr(req));
            throw new ServiceException("该支付通道上没有可进单的码");
        }

        //匹配码商费率
        List<SuitableQrcodeVO> match11 = match1.stream().filter(code ->
                code.getMerchantRate().compareTo(BigDecimal.ZERO) > 0
        ).collect(Collectors.toList());
        if (match11.isEmpty()) {
            log.error("码商的费率都为零，{}", JSONUtil.toJsonStr(req));
            throw new ServiceException("码商的费率都为零");
        }

        //根据下单金额匹配商户金额限制
        List<SuitableQrcodeVO> match2 = new ArrayList<>();
        for (SuitableQrcodeVO m1 : match11) {
            if (matchAmount(req.getAmount(), m1.getShopMinAmount(), m1.getShopMaxAmount())) {
                match2.add(m1);
            }
        }
        if (match2.isEmpty()) {
            log.error("下单金额超出商户设置的范围，{}", JSONUtil.toJsonStr(req));
            throw new ServiceException("下单金额超出商户设置的范围");
        }

        //根据下单金额匹配码商限制
        List<SuitableQrcodeVO> match3 = new ArrayList<>();
        for (SuitableQrcodeVO m2 : match2) {
            if (matchAmount(req.getAmount(), m2.getMerchantMinAmount(), m2.getMerchantMaxAmount())) {
                match3.add(m2);
            }
        }
        if (match3.isEmpty()) {
            log.error("下单金额超出码商设置的范围，{}", JSONUtil.toJsonStr(req));
            List<SuitableQrcodeVO> oneMerchant = match2.stream().collect(
                    Collectors.collectingAndThen(Collectors.toCollection(() ->
                            new TreeSet<>(Comparator.comparing(SuitableQrcodeVO::getMerchantName))), ArrayList::new));
            String merchantNames = oneMerchant.stream().map(SuitableQrcodeVO::getMerchantName).collect(Collectors.joining(","));
            throw new ServiceException("下单金额超出码商[" + merchantNames + "]可接单金额的范围");
        }

        //根据码商余额及最小接单金额过滤
        List<SuitableQrcodeVO> match4 = new ArrayList<>();
        match3.forEach(m3 -> {
            if (ObjectUtil.isNull(m3.getMinMerchantGetAmount()) || new BigDecimal(req.getAmount()).compareTo(m3.getMinMerchantGetAmount()) >= 0) {
                String mb = redisUtils.get(RedisKeys.merchantBalance + m3.getMerchantId());
                BigDecimal merchantBalance = new BigDecimal(StrUtil.isNotEmpty(mb)?mb:m3.getBalance());
                if (ObjectUtil.isNull(mb)) {
                    redisUtils.set(RedisKeys.merchantBalance + m3.getMerchantId(), merchantBalance);
                }
                BigDecimal yj = ObjectUtil.isNotEmpty(m3.getBaseDeposit()) ? m3.getBaseDeposit() : BigDecimal.ZERO;
                if (merchantBalance.subtract(new BigDecimal(req.getAmount())).subtract(yj).compareTo(BigDecimal.ZERO) >= 0) {
                    match4.add(m3);
                }
            }
        });
        if (match4.isEmpty()) {
            log.error("符合条件的码商余额都不足了，{}", JSONUtil.toJsonStr(req));
            List<SuitableQrcodeVO> oneMerchant = match3.stream().collect(
                    Collectors.collectingAndThen(Collectors.toCollection(() ->
                            new TreeSet<>(Comparator.comparing(SuitableQrcodeVO::getMerchantName))), ArrayList::new));
            String merchantNames = oneMerchant.stream().map(SuitableQrcodeVO::getMerchantName).collect(Collectors.joining(","));
            throw new ServiceException("码商[" + merchantNames + "]余额不足");
        }

        List<SuitableQrcodeVO> match5a = new ArrayList<>();
        match4.forEach(data -> {
            if (data.getDayLimit().compareTo(new BigDecimal("0")) <= 0 || new BigDecimal(req.getAmount()).compareTo(data.getDayLimit()) <= 0) {
                match5a.add(data);
            }
        });
        if (match5a.isEmpty()) {
            log.error("下单金额超出二维码日限额的范围，{}", JSONUtil.toJsonStr(req));
            List<SuitableQrcodeVO> oneMerchant = match4.stream().collect(
                    Collectors.collectingAndThen(Collectors.toCollection(() ->
                            new TreeSet<>(Comparator.comparing(SuitableQrcodeVO::getMerchantName))), ArrayList::new));
            String merchantNames = oneMerchant.stream().map(SuitableQrcodeVO::getMerchantName).collect(Collectors.joining(","));
            throw new ServiceException("下单金额超出码商[" + merchantNames + "]二维码日限额的范围");
        }

        //根据下单金额匹配单码限制
        List<SuitableQrcodeVO> match5 = new ArrayList<>();
        if (match5a.get(0).getSetAmount() == 0) {
            for (SuitableQrcodeVO m4 : match5a) {
                if (matchAmount(req.getAmount(), m4.getQrcodeMinAmount(), m4.getQrcodeMaxAmount())) {
                    match5.add(m4);
                }
            }
        } else {
            match5.addAll(match5a);
        }
        if (match5.isEmpty()) {
            log.error("下单金额超出二维码设置的范围，{}", JSONUtil.toJsonStr(req));
            List<SuitableQrcodeVO> oneMerchant = match5a.stream().collect(
                    Collectors.collectingAndThen(Collectors.toCollection(() ->
                            new TreeSet<>(Comparator.comparing(SuitableQrcodeVO::getMerchantName))), ArrayList::new));
            String merchantNames = oneMerchant.stream().map(SuitableQrcodeVO::getMerchantName).collect(Collectors.joining(","));
            throw new ServiceException("下单金额超出码商[" + merchantNames + "]二维码设置的范围");
        }

        //根据单码配置的日限额和笔数限制匹配
        List<SuitableQrcodeVO> match6 = new ArrayList<>();
        match5.forEach(qrcode -> {
            if (qrcode.getDayLimit().compareTo(BigDecimal.ZERO) == 0 && qrcode.getCountLimit() == 0) {
                match6.add(qrcode);
            } else {
                if (qrcode.getCountLimit() != 0) {
                    String oneKeys = RedisKeys.merchantQrcodeLimitCount + qrcode.getQrcodeId();
                    if (!redisUtils.hasKey(oneKeys)) {
                        redisUtils.set(oneKeys, "0", -1);
                    }
                    String nowLimit = redisUtils.get(oneKeys);
                    if ((new BigDecimal(nowLimit).add(new BigDecimal(1))).compareTo(new BigDecimal(qrcode.getCountLimit())) <= 0) {
                        match6.add(qrcode);
                    }
                }
                if (qrcode.getDayLimit().compareTo(BigDecimal.ZERO) != 0) {
                    String oneKeys = RedisKeys.merchantQrcodeLimitAmount + qrcode.getQrcodeId();
                    if (!redisUtils.hasKey(oneKeys)) {
                        redisUtils.set(oneKeys, "0", -1);
                    }
                    String nowLimit = redisUtils.get(oneKeys);
                    if (new BigDecimal(nowLimit).add(new BigDecimal(req.getAmount())).compareTo(qrcode.getDayLimit()) <= 0) {
                        match6.add(qrcode);
                    }
                }
            }
        });
        if (match6.isEmpty()) {
            log.error("下单金额超出码的日限额限制，请求参数：{}", JSONUtil.toJsonStr(req));
            List<SuitableQrcodeVO> oneMerchant = match5.stream().collect(
                    Collectors.collectingAndThen(Collectors.toCollection(() ->
                            new TreeSet<>(Comparator.comparing(SuitableQrcodeVO::getMerchantName))), ArrayList::new));
            String merchantNames = oneMerchant.stream().map(SuitableQrcodeVO::getMerchantName).collect(Collectors.joining(","));
            throw new ServiceException("下单金额超出码商[" + merchantNames + "]所有可进单码的日限额限制");
        }
        SuitableQrcodeVO lastQrcode = match6.get(0);
        List<SuitableQrcodeVO> list = match6;
        //根据权重获取数据
        Integer weightType = match6.get(0).getWeightType();
        if (weightType == null) {
            weightType = 1;
        }
        if (weightType == 0) {
           /* list = getListByWeight(match6);
            lastQrcode = list.get(0);*/
            lastQrcode = getQrcodeByPolling(match6, match6.get(0).getChannelId(), req.getMchid());
        } else if (weightType == 1){
            //根据码商去重
            List<SuitableQrcodeVO> match7 = match6.stream().collect(
                    Collectors.collectingAndThen(Collectors.toCollection(() ->
                            new TreeSet<>(Comparator.comparing(SuitableQrcodeVO::getMerchantId))), ArrayList::new));
            log.info("码商通道所有码商ID:{}",match7.stream().map(a -> a.getMerchantId().toString()).collect(Collectors.joining(",")));
            //根据轮询获取码商
            SuitableQrcodeVO match8 = getMerchantByPolling(match7, match7.get(0).getChannelId(), req.getMchid());
            log.info("轮询前码商的所有二维码ID:{}", JSONUtil.toJsonStr(list.stream().map(SuitableQrcodeVO::getQrcodeId).collect(Collectors.toList())));
            //获取该码商的所有码轮询
            list = list.stream().filter(a -> a.getMerchantId().equals(match8.getMerchantId())).sorted(Comparator.comparing(SuitableQrcodeVO::getQrcodeId)).distinct().collect(Collectors.toList());
            log.info("轮询后码商的所有二维码ID:{}", JSONUtil.toJsonStr(list.stream().map(SuitableQrcodeVO::getQrcodeId).distinct().collect(Collectors.toList())));
            lastQrcode = getMerchantQrcodePolling(list, match8);
        } else if (weightType == 2){
            lastQrcode = getListByWeight(match6);
        } else if (weightType == 3){
            //根据码商去重
            List<SuitableQrcodeVO> match7 = match6.stream().collect(
                    Collectors.collectingAndThen(Collectors.toCollection(() ->
                            new TreeSet<>(Comparator.comparing(SuitableQrcodeVO::getMerchantId))), ArrayList::new));
            //根据权重选举出码商
            SuitableQrcodeVO match8 = getListByWeightMerchant(match7);
            log.info("权重过滤前的所有二维码ID:{}", JSONUtil.toJsonStr(list.stream().map(SuitableQrcodeVO::getQrcodeId).collect(Collectors.toList())));
            //获取该码商的所有码轮询
            list = list.stream().filter(a -> a.getMerchantId().equals(match8.getMerchantId())).sorted(Comparator.comparing(SuitableQrcodeVO::getQrcodeId)).distinct().collect(Collectors.toList());
            log.info("权重过滤后的所有二维码ID:{}", JSONUtil.toJsonStr(list.stream().map(SuitableQrcodeVO::getQrcodeId).distinct().collect(Collectors.toList())));
           lastQrcode = getMerchantQrcodePolling(list, match8);
        }

        BigDecimal fixedAmount = new BigDecimal(req.getAmount());
        //判断是否开启锁码
        if (lastQrcode.getLockedCode() == 0 && lastQrcode.getOverAmount() != 0) {
            Boolean locked = redisLock.getLock(RedisKeys.lockedQrcodeLock + lastQrcode.getQrcodeId(), "1");
            if (locked) {
                lastQrcode = getLastQrcode(list, lastQrcode);
            }
        } else if (lastQrcode.getLockedCode() == 0) {
            lastQrcode = getLastQrcodeLast(list, fixedAmount, lastQrcode);
            fixedAmount = lastQrcode.getFixAmount();
        } else if (lastQrcode.getOverAmount() == 0){
            lastQrcode = getLastQrcodeAmount(list, fixedAmount, lastQrcode);
            fixedAmount = lastQrcode.getFixAmount();
        }

        //该码商没有码符合条件，则从前面匹配到的所有码商中重新筛选
        if (lastQrcode == null || ObjectUtil.isNull(lastQrcode.getQrcodeId())) {
            match6.remove(lastQrcode);
            lastQrcode = list.get(0);
            fixedAmount = new BigDecimal(req.getAmount());
            if (lastQrcode.getLockedCode() == 0 && lastQrcode.getOverAmount() != 0) {
                lastQrcode = getLastQrcode(match6, lastQrcode);
            } else if (lastQrcode.getLockedCode() == 0) {
                lastQrcode = getLastQrcodeLast(match6, fixedAmount, lastQrcode);
                fixedAmount = lastQrcode.getFixAmount();
            } else if (lastQrcode.getOverAmount() == 0){
                lastQrcode = getLastQrcodeAmount(match6, fixedAmount, lastQrcode);
                fixedAmount = lastQrcode.getFixAmount();
            }
        }

        if (lastQrcode.getOverAmountRate() != null && lastQrcode.getOverAmountRate() != 0 && lastQrcode.getOverAmount() == 0) {
            BigDecimal subAmount = (new BigDecimal(req.getAmount()).subtract(fixedAmount)).multiply(new BigDecimal(lastQrcode.getOverAmountRate()));
            fixedAmount = new BigDecimal(req.getAmount()).subtract(subAmount);
        }

        if (ObjectUtil.isNull(lastQrcode.getQrcodeId())) {
            log.info("所有码都已超过浮动或被锁码");
            String merchantNames = match6.stream().map(SuitableQrcodeVO::getMerchantName).collect(Collectors.joining(","));
            throw new ServiceException("所有码商["+merchantNames+"]的可进单码都已超过浮动或被锁码");
        }
        BigDecimal agentChange = new BigDecimal(req.getAmount()).multiply(lastQrcode.getAgentRate().divide(new BigDecimal(100),4, RoundingMode.HALF_UP));
        if (agentChange.compareTo(new BigDecimal(redisUtils.get(RedisKeys.agentBalance + lastQrcode.getAgentId()))) > 0) {
            return AjaxResult.error(1,"余额不足");
        }
        if (iCtOnebuyBlackService.checkPayer(req.getBody()) || iCtOnebuyBlackService.checkPayer(clientIp)) {
            throw new ServiceException("下单失败：该用户或IP已被加入黑名单");
        }

        /*创建订单*/
        //订单ID
        String tradeNo = "HB" + snowflakeIdService.nextId();

        // 处理一码通
        OnebuyCreateOrderRes onebuyOrder;
        if (lastQrcode.getChannelId() != null && ChannelConstants.CHANNEL_ID_ONEBUY == lastQrcode.getChannelId()) {

            onebuyOrder = onebuyService.createOnebuyOrder(tradeNo, String.valueOf(new BigDecimal(req.getAmount()).multiply(BigDecimal.valueOf(100))), match6.stream().map(SuitableQrcodeVO::getAccountNumber).collect(Collectors.toList()));
            // 从码列表里面找对象
            List<SuitableQrcodeVO> onebuyQrcodeList = shopMatchList.stream().filter(code ->
                    StrUtil.equals(code.getAccountNumber(), onebuyOrder.getShop_id())
            ).collect(Collectors.toList());
            if (CollUtil.isEmpty(onebuyQrcodeList)) {
                throw new ServiceException("一码通下单失败：码不存在");
            }
            lastQrcode = onebuyQrcodeList.get(0);
        } else {
            onebuyOrder = null;
        }

        InOrderEntity inOrderEntity = InOrderEntity.builder()
                .shopOrderNo(req.getOut_trade_no())
                .tradeNo(tradeNo)
                .qrcodeId(lastQrcode.getQrcodeId())
                .nickName(lastQrcode.getNickName())
                .accountNumber(lastQrcode.getAccountNumber())
                .uid(lastQrcode.getUid())
                .qrcodeType(lastQrcode.getQrcodeType())
                .qrcodeUrl(lastQrcode.getQrcodeUrl())
                .qrcodeValue(lastQrcode.getQrcodeValue())
                .accountRemark(lastQrcode.getAccountRemark())
                .merchantId(lastQrcode.getMerchantId())
                .merchantName(lastQrcode.getMerchantName())
                .channelId(lastQrcode.getChannelId())
                .channelName(lastQrcode.getChannelName())
                .orderAmount(new BigDecimal(req.getAmount()))
                .fixedAmount(fixedAmount)
                .orderTime(new Date())
                .finishTime(new Date())
                .orderStatus(OrderStatus.WAIT.getValue())
                .callbackStatus(0)
                .clientIp(clientIp)
                .payer(req.getBody())
                .opcoin(0)
                .build();
        lastQrcode.setNotify_url(req.getNotify_url());
        String forwardUrl = sysConfigService.selectConfigByKey(BusinessConfigKey.FORWARD_URL.getConfigKey())
                +"?orderNo=" + tradeNo + "&qrcodeModel=" + lastQrcode.getQrcodeModel();
        lastQrcode.setForwordUrl(forwardUrl);

        // 处理一码通
        if (lastQrcode.getChannelId() != null && ChannelConstants.CHANNEL_ID_ONEBUY == lastQrcode.getChannelId() && onebuyOrder != null) {
            lastQrcode.setForwordUrl(onebuyOrder.getUrl());
            inOrderEntity.setOnebuyOrderNo(onebuyOrder.getId());
        }
        boolean flag = orderService.saveOrder(inOrderEntity, lastQrcode);
        if (flag) {
           Map<String, Object> map = new HashMap<>();
            map.put("request_url", lastQrcode.getForwordUrl());
            map.put("money", inOrderEntity.getOrderAmount());
            map.put("pay_price", inOrderEntity.getFixedAmount());
            map.put("account_name", lastQrcode.getNickName());
            map.put("bank_name", lastQrcode.getUid());
            map.put("bank_zhihang", lastQrcode.getAccountRemark());
            map.put("account_number", lastQrcode.getAccountNumber());
            map.put("mchId", inOrderEntity.getShopOrderNo());
            map.put("tradeNo", tradeNo);
           return AjaxResult.success(0, "下单成功", map);
        }
        throw new ServiceException("创建订单失败");

    }

    public AjaxResult createOrderNew(ShopOrderReq req, String clientIp){
        if (StrUtil.isEmpty(req.getMchid())) {
            throw new ServiceException("商户id为空");
        }
        if (StrUtil.isEmpty(req.getOut_trade_no())) {
            throw new ServiceException("订单号为空");
        }
        if (StrUtil.isEmpty(req.getAmount())) {
            throw new ServiceException("下单金额为空");
        }
        if (StrUtil.isEmpty(req.getChannel())) {
            throw new ServiceException("支付通道编码为空");
        }
        if (StrUtil.isEmpty(req.getNotify_url())) {
            throw new ServiceException("回调通知地址为空");
        }
        if (StrUtil.isEmpty(req.getReturn_url())) {
            throw new ServiceException("同步通知地址为空");
        }
        if (StrUtil.isEmpty(req.getTime_stamp())) {
            throw new ServiceException("时间为空");
        }
        if (StrUtil.isEmpty(req.getBody())) {
            throw new ServiceException("body为空");
        }
        if (StrUtil.isEmpty(req.getSign())) {
            throw new ServiceException("签名为空");
        }
        /*查找合适的码*/
        String shopStr = redisUtils.get(RedisKeys.shopInfo);
        if (StrUtil.isEmpty(shopStr)) {
            return AjaxResult.error("没有可用的商户");
        }
        List<ShopEntity> shopList = JSONUtil.toList(shopStr, ShopEntity.class);
        ShopEntity shopEntity = shopList.stream().filter(shop ->
                shop.getUserId().toString().equals(req.getMchid()) &&
                        shop.getStatus() == 0
        ).findFirst().orElse(null);
        if (shopEntity == null) {
            log.error("商户Id错误，请求参数{}", JSONUtil.toJsonStr(req));
            return AjaxResult.error("商户Id错误");
        }
        //过滤商户最大最小金额
        if (!matchAmount(req.getAmount(), shopEntity.getMinAmount(), shopEntity.getMaxAmount())) {
            log.error("商户最大最小金额不匹配，请求参数{}", JSONUtil.toJsonStr(req));
            return AjaxResult.error("没有可用的二维码");
        }
        /*验证签名*/
        String shopSignSecret = shopEntity.getSignSecret();
        Map<String, Object> mapreq = BeanUtil.beanToMap(req);
        mapreq.remove("sign");
        String signStr = DESUtil.getSingByMap(mapreq) + "&key=" + shopSignSecret;
        String signNew = DigestUtil.md5Hex(signStr);
        if (!StrUtil.equals(signNew.toLowerCase(), req.getSign())) {
            log.error("签名有误，{}", JSONUtil.toJsonStr(req));
            return AjaxResult.error("签名有误");
        }
        //通道
        List<ChannelEntity> channelList = JSONUtil.toList(redisUtils.get(RedisKeys.channelInfo), ChannelEntity.class);
        ChannelEntity channel = channelList.stream().filter(ch -> ch.getChannelCode().equals(req.getChannel())).findFirst().orElse(null);
        if (channel == null) {
            log.error("通道错误，{}", JSONUtil.toJsonStr(req));
            return AjaxResult.error("通道错误");
        }
        //根据通道编号匹配
        String shopChannelStr = redisUtils.get(RedisKeys.shopChannelInfo);
        if (StrUtil.isEmpty(shopChannelStr)) {
            return AjaxResult.error("没有可用的二维码");
        }
        List<ShopBaseChannelEntity> shopChannelList = JSONUtil.toList(shopChannelStr, ShopBaseChannelEntity.class);
        ShopBaseChannelEntity shopBaseChannelEntity = shopChannelList.stream().filter(shopc ->
                shopc.getChannelId().equals(channel.getId()) && shopc.getStatus() == 0
                && shopc.getShopId().toString().equals(req.getMchid())
        ).findFirst().orElse(null);
        if (shopBaseChannelEntity == null) {
            log.error("支付通道编码错误，{}", JSONUtil.toJsonStr(req));
            return AjaxResult.error("没有可用的二维码");
        }
        //获取产品
        List<AgentChannelEntity> agentChannelList = JSONUtil.toList(redisUtils.get(RedisKeys.agentChannel), AgentChannelEntity.class);
        AgentChannelEntity agentChannel = agentChannelList.stream().filter(agent ->
                agent.getChannelId().equals(channel.getId()) && agent.getStatus() == 0
                && agent.getAgentId().equals(shopEntity.getAgentId())
                ).findFirst().orElse(null);
        //判断代理余额是否充足
        if (agentChannel != null) {
//            BigDecimal agentChange = new BigDecimal(req.getAmount()).multiply(agentChannel.getCostRate().divide(new BigDecimal(100), 4, RoundingMode.HALF_UP));
//            String angetBalance = redisUtils.get(RedisKeys.agentBalance + agentChannel.getAgentId());
//            if (agentChange.compareTo(new BigDecimal(angetBalance)) > 0) {
//                log.error("代理余额不足：代理ID：{}应扣减金额{}, 余额：{}",agentChannel.getAgentId(), agentChange, angetBalance);
//                return AjaxResult.error(1, "余额不足");
//            }
        }else {
            log.error("未找到代理");
            return AjaxResult.error(1, "没有可用的二维码");
        }
        //获取该商户配置的码商
        String shopMerchantStr = redisUtils.get(RedisKeys.shopMerchantInfo);
        if (StrUtil.isEmpty(shopMerchantStr)) {
            log.error("该商户没有配置任何码商，{}", JSONUtil.toJsonStr(req));
            return AjaxResult.error("没有可用的二维码");
        }
        //过滤码商
        String merchantStr = redisUtils.get(RedisKeys.merchantInfo);
        if (StrUtil.isEmpty(merchantStr)) {
            log.error("没有可用的码商，{}", JSONUtil.toJsonStr(req));
            return AjaxResult.error("没有可用的二维码");
        }
        //商户配置的码商
        List<ShopMerchantRelationEntity> smrList = JSONUtil.toList(shopMerchantStr, ShopMerchantRelationEntity.class);
        //所有配置在商户上的码商
        List<Long> merchantIds = smrList.stream().filter(sm -> sm.getShopId().equals(shopEntity.getUserId()))
                .map(ShopMerchantRelationEntity::getMerchantId).collect(Collectors.toList());
        //获取码商
        List<MerchantEntity> merchantList = JSONUtil.toList(merchantStr, MerchantEntity.class);
        //过滤码商开工状态，账号状态、最小接单金额和接单状态以及商户配置的码商
        List<MerchantEntity> merchantStatus = merchantList.stream().filter(mc ->
                mc.getWorkStatus() == 0 && mc.getStatus()==0 && mc.getOrderPermission() == 0
                && mc.getMinAmount().compareTo(new BigDecimal(req.getAmount())) <= 0
                && mc.getTeamStatus() == 0
                && merchantIds.contains(mc.getUserId())
                ).collect(Collectors.toList());
        if (merchantStatus.isEmpty()) {
            log.error("码商均未开启进单状态，{}", JSONUtil.toJsonStr(req));
            return AjaxResult.error("没有可用的二维码");
        }
        //码商余额过滤
        List<MerchantEntity> merchantAmountList = new ArrayList<>();
        for (MerchantEntity merchant : merchantStatus) {
            String balance = redisUtils.get(RedisKeys.merchantBalance + merchant.getUserId());
            if (StrUtil.isNotEmpty(balance) && new BigDecimal(balance).compareTo(new BigDecimal(req.getAmount())) >= 0) {
                merchantAmountList.add(merchant);
            }
        }
        if (merchantAmountList.isEmpty()) {
            log.error("所有符合条件的码商余额都不足，{}", JSONUtil.toJsonStr(req));
            return AjaxResult.error("没有可用的二维码");
        }
        //过滤码商通道
        String merchantChannelStr = redisUtils.get(RedisKeys.merchantChannelInfo);
        if (StrUtil.isEmpty(merchantChannelStr)) {
            log.error("码商通道未找到，{}", JSONUtil.toJsonStr(req));
            return AjaxResult.error("没有可用的二维码");
        }
        //过滤码商通道、开启状态、最大最小金额、费率设置
        List<MerchantChannelEntity> merchantChannelList = JSONUtil.toList(merchantChannelStr, MerchantChannelEntity.class);
        List<MerchantChannelEntity> mcList = merchantChannelList.stream().filter(mc ->
                        merchantAmountList.stream().map(MerchantEntity::getUserId).collect(Collectors.toList()).contains(mc.getMerchantId())
                        && mc.getStatus() == 0 && mc.getChannelRate().compareTo(BigDecimal.ZERO) > 0
                        && (mc.getMinAmount().compareTo(BigDecimal.ZERO) == 0 || mc.getMinAmount().compareTo(new BigDecimal(req.getAmount())) <= 0)
                                && (mc.getMaxAmount().compareTo(BigDecimal.ZERO) == 0 || mc.getMaxAmount().compareTo(new BigDecimal(req.getAmount())) >= 0)
                ).collect(Collectors.toList());
        if (mcList.isEmpty()) {
            log.error("码商费率或者最大最小金额不匹配，{}", JSONUtil.toJsonStr(req));
            return AjaxResult.error("没有可用的二维码");
        }

        //码商的通道配置ID集合
        List<Long> merchantChannelIds = mcList.stream().map(MerchantChannelEntity::getId).collect(Collectors.toList());

        //获取码商的二维码
        String qrcodeStr = redisUtils.get(RedisKeys.merchantQrcode);
        if (StrUtil.isEmpty(qrcodeStr)) {
            log.error("没有二维码可用，{}", JSONUtil.toJsonStr(req));
            return AjaxResult.error("没有可用的二维码");
        }

        //获取二维码
        List<MerchantQrcodeEntity> qrcodeList = JSONUtil.toList(qrcodeStr, MerchantQrcodeEntity.class);
        //过滤码商通道上的二维码、二维码设置信息
        List<MerchantQrcodeEntity>  fiterQrcodeList = qrcodeList.stream().filter(qrcode ->
                merchantChannelIds.contains(qrcode.getId()) && qrcode.getStatus() == 0
                        &&(qrcode.getMinAmount().compareTo(BigDecimal.ZERO) == 0 || qrcode.getMinAmount().compareTo(new BigDecimal(req.getAmount())) <= 0)
                        &&(qrcode.getMaxAmount().compareTo(BigDecimal.ZERO) == 0 || qrcode.getMaxAmount().compareTo(new BigDecimal(req.getAmount())) >= 0)
        ).collect(Collectors.toList());
        if (fiterQrcodeList.isEmpty()) {
            log.error("没有二维码可用，{}", JSONUtil.toJsonStr(req));
            return AjaxResult.error("没有可用的二维码");
        }


        //根据单码配置的日限额和笔数限制匹配
        List<MerchantQrcodeEntity> xianeList = new ArrayList<>();
        fiterQrcodeList.forEach(qrcode -> {
            if (qrcode.getDayLimit().compareTo(BigDecimal.ZERO) == 0 && qrcode.getCountLimit() == 0) {
                xianeList.add(qrcode);
            } else if (qrcode.getCountLimit() != 0){
                String oneKeys = RedisKeys.merchantQrcodeLimitCount + qrcode.getId();
                if (!redisUtils.hasKey(oneKeys)) {
                    redisUtils.set(oneKeys, "0");
                }
                String nowLimit = redisUtils.get(oneKeys);
                if ((new BigDecimal(nowLimit).add(new BigDecimal(1))).compareTo(new BigDecimal(qrcode.getCountLimit())) <= 0) {
                    xianeList.add(qrcode);
                }
            } else if (qrcode.getDayLimit().compareTo(BigDecimal.ZERO) != 0) {
                String oneKeys = RedisKeys.merchantQrcodeLimitAmount + qrcode.getId();
                if (!redisUtils.hasKey(oneKeys)) {
                    redisUtils.set(oneKeys, "0");
                }
                String nowLimit = redisUtils.get(oneKeys);
                if (new BigDecimal(nowLimit).add(new BigDecimal(req.getAmount())).compareTo(qrcode.getDayLimit()) <= 0) {
                    xianeList.add(qrcode);
                }
            }
        });
        if (xianeList.isEmpty()) {
            log.error("下单金额超出码的日限额限制，请求参数：{}", JSONUtil.toJsonStr(req));
            throw new ServiceException("没有可用的二维码");
        }
       //锁码过滤-----//todo暂时没有锁码
        List<MerchantQrcodeEntity> unLockQrcodeList = new ArrayList<>();
        xianeList.forEach(qrcode -> {
            AgentChannelEntity agentChannelEntity = agentChannelList.stream()
                    .filter(ac -> ac.getAgentId().equals(qrcode.getAgentId())
                    && ac.getChannelId().equals(qrcode.getChannelId())
                            && ac.getStatus() == 0)
                    .findFirst().orElse(null);
            if (agentChannelEntity ==null || agentChannelEntity.getLockedCode() != 0 ||!redisUtils.hasKey(RedisKeys.lockedQrcode + qrcode.getId())) {
                unLockQrcodeList.add(qrcode);
            }
        });
        if (unLockQrcodeList.isEmpty()) {
            log.error("所有码均已被锁码，请求参数：{}", JSONUtil.toJsonStr(req));
            throw new ServiceException("没有可用的二维码");
        }
        //浮动金额过滤
        List<MerchantQrcodeEntity> fixedList = new ArrayList<>();
        unLockQrcodeList.forEach(qrcode -> {
            String redisKey = qrcode.getMerchantId() + ":" + qrcode.getChannelId() +  ":" + req.getAmount();
            if (!redisUtils.hasKey(redisKey)) {
                fixedList.add(qrcode);
            } else {
                List<Object> fixedAmount = redisUtils.range(redisKey,0,100);
                if (agentChannel.getOverAmount() == 1 || ObjectUtil.isEmpty(fixedAmount) || fixedAmount.size() <= agentChannel.getOverAmountValue()) {
                    fixedList.add(qrcode);
                } else {
                    fixedList.add(qrcode);
                }
            }
        });
        if (fixedList.isEmpty()) {
            log.error("所有码都超过浮动金额，请求参数：{}", JSONUtil.toJsonStr(req));
            throw new ServiceException("没有可用的二维码");
        }
        List<MerchantChannelEntity> resultList = mcList.stream().filter(mc ->
                fixedList.stream().map(MerchantQrcodeEntity::getMerchantChannelId).collect(Collectors.toList()).contains(mc.getId())
        ).collect(Collectors.toList());

        //根据码商通道权重选举
        MerchantChannelEntity merchantChannel = WeightedRandomSelectorNew.selectWeightedRandomly(resultList);
        //再根据选举拿到码
        MerchantQrcodeEntity merchantQrcode = fixedList.stream().filter(qrcode ->
                qrcode.getMerchantChannelId().equals(merchantChannel.getId())).findFirst().orElse(fixedList.get(0));
        //码商
        MerchantQrcodeEntity finalMerchantQrcode = merchantQrcode;
        MerchantEntity merchantEntity = merchantList.stream().filter(me -> me.getUserId().equals(finalMerchantQrcode.getMerchantId())).findFirst().orElse(null);
        if (merchantEntity == null) {
            return AjaxResult.error("没有合适的二维码");
        }
        if (iCtOnebuyBlackService.checkPayer(req.getBody()) || iCtOnebuyBlackService.checkPayer(clientIp)) {
            throw new ServiceException("下单失败：该用户或IP已被加入黑名单");
        }

        //订单ID
        String tradeNo = "HB" + snowflakeIdService.nextId();

        // 处理一码通
        OnebuyCreateOrderRes onebuyOrder;
        if (merchantQrcode.getChannelId() != null && ChannelConstants.CHANNEL_ID_ONEBUY == merchantQrcode.getChannelId()) {

            onebuyOrder = onebuyService.createOnebuyOrder(tradeNo, String.valueOf(new BigDecimal(req.getAmount()).multiply(BigDecimal.valueOf(100))),
                    fixedList.stream().filter(qrcode ->
                            qrcode.getMerchantChannelId().equals(merchantChannel.getId())).collect(Collectors.toList()).stream().map(MerchantQrcodeEntity::getAccountNumber).collect(Collectors.toList()));
            // 从码列表里面找对象
            List<MerchantQrcodeEntity> onebuyQrcodeList = fixedList.stream().filter(code ->
                    StrUtil.equals(code.getAccountNumber(), onebuyOrder.getShop_id())
            ).collect(Collectors.toList());
            if (CollUtil.isEmpty(onebuyQrcodeList)) {
                throw new ServiceException("一码通下单失败：码不存在");
            }
            merchantQrcode = onebuyQrcodeList.get(0);
        } else {
            onebuyOrder = null;
        }

        /*创建订单*/
        String redisKey = merchantQrcode.getMerchantId() + ":" + merchantQrcode.getChannelId() + ":" + req.getAmount();
        long orderOvertime = merchantChannel.getMerchantOvertime() != 0 ? merchantChannel.getMerchantOvertime() : agentChannel.getOvertime();
        double fa = new BigDecimal(req.getAmount()).doubleValue();
        if (agentChannel.getOverAmount() == 0) {
            fa = redisUtils.processOrder(redisKey, orderOvertime * 60, agentChannel.getOverAmountValue());
        }
        InOrderEntity inOrderEntity = InOrderEntity.builder()
                .shopOrderNo(req.getOut_trade_no())
                .tradeNo(tradeNo)
                .qrcodeId(merchantQrcode.getId())
                .nickName(merchantQrcode.getNickName())
                .accountNumber(merchantQrcode.getAccountNumber())
                .uid(merchantQrcode.getUid())
                .qrcodeType(merchantQrcode.getQrcodeType())
                .qrcodeUrl(merchantQrcode.getQrcodeUrl())
                .qrcodeValue(merchantQrcode.getQrcodeValue())
                .accountRemark(merchantQrcode.getAccountRemark())
                .merchantId(merchantQrcode.getMerchantId())
                .merchantName(merchantEntity.getUserName())
                .channelId(merchantQrcode.getChannelId())
                .channelName(merchantQrcode.getChannelName())
                .orderAmount(new BigDecimal(req.getAmount()))
                .fixedAmount(BigDecimal.valueOf(fa))
                .orderTime(new Date())
                .orderStatus(OrderStatus.WAIT.getValue())
                .callbackStatus(0)
                .clientIp(clientIp)
                .opcoin(0)
                .build();
        SuitableQrcodeVO lastQrcode = new SuitableQrcodeVO();
        lastQrcode.setQrcodeId(merchantQrcode.getId());
        lastQrcode.setMerchantOvertime(orderOvertime);
        lastQrcode.setOvertime(orderOvertime);
        lastQrcode.setShopId(shopEntity.getUserId());
        lastQrcode.setShopName(shopEntity.getUserName());
        lastQrcode.setAgentId(agentChannel.getAgentId());
        lastQrcode.setMerchantRate(merchantChannel.getChannelRate());
        lastQrcode.setShopRate(shopBaseChannelEntity.getChannelRate());
        lastQrcode.setAgentRate(agentChannel.getCostRate());
        lastQrcode.setChannelId(channel.getId());
        lastQrcode.setChannelCode(channel.getChannelCode());
        lastQrcode.setChannelName(channel.getChannelName());
        lastQrcode.setNotify_url(req.getNotify_url());
        lastQrcode.setMerchantChannelId(merchantQrcode.getMerchantChannelId());
        lastQrcode.setAgentChannelId(agentChannel.getId());
        lastQrcode.setMerchantRateOne(merchantChannel.getMerchantRateOne());
        lastQrcode.setMerchantRateTwo(merchantChannel.getMerchantRateTwo());
        lastQrcode.setMerchantRateThree(merchantChannel.getMerchantRateThree());
        lastQrcode.setMerchantRateFour(merchantChannel.getMerchantRateFour());
        lastQrcode.setMerchantRateFive(merchantChannel.getMerchantRateFive());
        lastQrcode.setLockedCode(agentChannel.getLockedCode());
        lastQrcode.setOrderRemind(merchantEntity.getOrderRemind());
        String forwardUrl = sysConfigService.selectConfigByKey(BusinessConfigKey.FORWARD_URL.getConfigKey())
                +"?orderNo=" + tradeNo + "&qrcodeModel=" + lastQrcode.getQrcodeModel();
        lastQrcode.setForwordUrl(forwardUrl);

        // 处理一码通
        if (lastQrcode.getChannelId() != null && ChannelConstants.CHANNEL_ID_ONEBUY == lastQrcode.getChannelId() && onebuyOrder != null) {
            lastQrcode.setForwordUrl(onebuyOrder.getUrl());
            inOrderEntity.setOnebuyOrderNo(onebuyOrder.getId());
        }

        boolean flag = orderService.saveOrder(inOrderEntity, lastQrcode);
        if (flag) {
            Map<String, Object> map = new HashMap<>();
            map.put("request_url", lastQrcode.getForwordUrl());
            map.put("money", inOrderEntity.getOrderAmount());
            map.put("pay_price", inOrderEntity.getFixedAmount());
            map.put("account_name", lastQrcode.getNickName());
            map.put("bank_name", lastQrcode.getUid());
            map.put("bank_zhihang", lastQrcode.getAccountRemark());
            map.put("account_number", lastQrcode.getAccountNumber());
            map.put("mchId", inOrderEntity.getShopOrderNo());
            map.put("tradeNo", tradeNo);
            return AjaxResult.success(0, "下单成功", map);
        }
        throw new ServiceException("创建订单失败");

    }

    public SuitableQrcodeVO getLastQrcode(List<SuitableQrcodeVO> list, SuitableQrcodeVO lastQrcode) {
        if (lastQrcode == null) {
            lastQrcode = new SuitableQrcodeVO();
        }
        for (SuitableQrcodeVO suitableQrcodeVO : list) {
            try {
                Boolean locked = redisLock.getLock(RedisKeys.lockedQrcodeLock + suitableQrcodeVO.getQrcodeId(), "1");
                if (locked) {
                    if (redisUtils.hasKey(RedisKeys.lockedQrcode + suitableQrcodeVO.getQrcodeId())) {
                        //码被锁定，释放锁，循环拿下一条
                        redisLock.releaseLock(RedisKeys.lockedQrcodeLock + suitableQrcodeVO.getQrcodeId(), "1");
                        log.error("该码已被锁定，重新获取码");
                    } else {
                        lastQrcode = suitableQrcodeVO;
                        //锁码
                        redisUtils.set(RedisKeys.lockedQrcode + lastQrcode.getQrcodeId(), "1", (long) lastQrcode.getOvertime() * 60);
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("获取码异常，{}", e.getMessage());
            }finally {
                redisLock.releaseLock(RedisKeys.lockedQrcodeLock + lastQrcode.getQrcodeId(), "1");
            }
        }
        return lastQrcode;
    }

    public SuitableQrcodeVO getLastQrcodeLast(List<SuitableQrcodeVO> list, BigDecimal fixedAmount, SuitableQrcodeVO lastQrcode) {
        if (lastQrcode != null) {
            long orderOvertime = lastQrcode.getMerchantOvertime() != 0 ? lastQrcode.getMerchantOvertime() : lastQrcode.getOvertime();
            try {
                Boolean locked = redisLock.getLock(RedisKeys.lockedQrcodeLock + lastQrcode.getQrcodeId(), "1");
                if (locked) {
                    if (!redisUtils.hasKey(RedisKeys.lockedQrcode + lastQrcode.getQrcodeId())) {
                        lastQrcode = BeanUtil.copyProperties(lastQrcode, SuitableQrcodeVO.class);
//                        String redisKey = lastQrcode.getMerchantId() + ":" + lastQrcode.getChannelId() + ":" + lastQrcode.getQrcodeId() + ":" + fixedAmount;
                        String redisKey = lastQrcode.getMerchantId() + ":" + lastQrcode.getChannelId() + ":"  + fixedAmount;
                        double fa = redisUtils.processOrder(redisKey, orderOvertime * 60, lastQrcode.getOverAmountValue());
                        if (fa != 0) {
                            BigDecimal lastAmount = fixedAmount.subtract(BigDecimal.valueOf(fa).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                            lastQrcode.setFixAmount(lastAmount);
                        }
                        //锁码
                        redisUtils.set(RedisKeys.lockedQrcode + lastQrcode.getQrcodeId(), "1", orderOvertime * 60);
                        return lastQrcode;
                    }
                }
            } catch (Exception e) {
                log.error("获取码异常，{}", e.getMessage());
            }finally {
                redisLock.releaseLock(RedisKeys.lockedQrcodeLock + lastQrcode.getQrcodeId(), "1");
            }
        }else {
            lastQrcode = new SuitableQrcodeVO();
        }
        for (SuitableQrcodeVO suitableQrcodeVO : list) {
            long orderOvertime = suitableQrcodeVO.getMerchantOvertime() != 0 ? suitableQrcodeVO.getMerchantOvertime() : suitableQrcodeVO.getOvertime();
            try {
                Boolean locked = redisLock.getLock(RedisKeys.lockedQrcodeLock + suitableQrcodeVO.getQrcodeId(), "1");
                if (locked) {
                    if (!redisUtils.hasKey(RedisKeys.lockedQrcode + suitableQrcodeVO.getQrcodeId())) {
                        lastQrcode = BeanUtil.copyProperties(suitableQrcodeVO, SuitableQrcodeVO.class);
//                        String redisKey = lastQrcode.getMerchantId() + ":" + lastQrcode.getChannelId() + ":" + lastQrcode.getQrcodeId() + ":" + fixedAmount;
                        String redisKey = lastQrcode.getMerchantId() + ":" + lastQrcode.getChannelId() + ":"  + fixedAmount;
                        double fa = redisUtils.processOrder(redisKey, orderOvertime * 60, lastQrcode.getOverAmountValue());
                        if (fa != 0) {
                            BigDecimal lastAmount = fixedAmount.subtract(BigDecimal.valueOf(fa).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                            lastQrcode.setFixAmount(lastAmount);
                        }
                        //锁码
                        redisUtils.set(RedisKeys.lockedQrcode + lastQrcode.getQrcodeId(), "1", orderOvertime * 60);
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("获取码异常，{}", e.getMessage());
            }finally {
                redisLock.releaseLock(RedisKeys.lockedQrcodeLock + lastQrcode.getQrcodeId(), "1");
            }
        }
        return lastQrcode;
    }

    public SuitableQrcodeVO getLastQrcodeAmount(List<SuitableQrcodeVO> list, BigDecimal fixedAmount, SuitableQrcodeVO vo) {
        if (vo != null) {
            long orderOvertime = vo.getMerchantOvertime() != 0 ? vo.getMerchantOvertime() : vo.getOvertime();
            String redisKey = vo.getMerchantId() + ":" + vo.getChannelId() + ":" + fixedAmount;
            double fa = redisUtils.processOrder(redisKey, orderOvertime * 60, vo.getOverAmountValue());
            if (fa != 0) {
                BigDecimal lastAmount = fixedAmount.subtract(BigDecimal.valueOf(fa).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                vo.setFixAmount(lastAmount);
                return vo;
            }
        }
        vo = new SuitableQrcodeVO();
        for (SuitableQrcodeVO lastQrcode : list) {
            //订单过期时长（分钟）
            long orderOvertime = lastQrcode.getMerchantOvertime() != 0 ? lastQrcode.getMerchantOvertime() : lastQrcode.getOvertime();
            String redisKey = lastQrcode.getMerchantId() + ":" + lastQrcode.getChannelId() + ":" + fixedAmount;
            double fa = redisUtils.processOrder(redisKey, orderOvertime * 60, lastQrcode.getOverAmountValue());
            if (fa != 0) {
                BigDecimal lastAmount = fixedAmount.subtract(BigDecimal.valueOf(fa).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                vo = lastQrcode;
                vo.setFixAmount(lastAmount);
                break;
            }
        }
        return vo;
    }

    public boolean matchAmount(String reqAmount, BigDecimal minAmount, BigDecimal maxAmount) {
        if (minAmount.compareTo(BigDecimal.ZERO) == 0 || new BigDecimal(reqAmount).compareTo(minAmount) >= 0) {
            //如果商户最大金额为空或者下单金额小于最大金额
            return maxAmount.compareTo(BigDecimal.ZERO) == 0 || new BigDecimal(reqAmount).compareTo(maxAmount) <= 0;
        }
        return false;
    }

    public SuitableQrcodeVO getListByWeight(List<SuitableQrcodeVO> list){
        return WeightedRandomSelector.selectWeightedRandomly(list);
    }

    public SuitableQrcodeVO getListByWeightMerchant(List<SuitableQrcodeVO> list){
        return WeightedRandomSelectorMerchant.selectWeightedRandomly(list);
    }

    public SuitableQrcodeVO getQrcodeByPolling(List<SuitableQrcodeVO> list, Long channelId, String shopId){
/*
        SuitableQrcodeVO result = list.get(0);
        if (list.size() == 1) {
            return result;
        }
        //根据码商二维码数量作为权重值进行权重筛选
        try {
            Map<Long, List<SuitableQrcodeVO>> map = list.stream().collect(Collectors.groupingBy(SuitableQrcodeVO::getMerchantId));
            //权重法获取码商,根据码商二维码数量作为权重值
            WeightRandom<Long> weightObjs = new WeightRandom<>();
            for (Long key : map.keySet()) {
                List<SuitableQrcodeVO> value = map.get(key);
                weightObjs.add(key, value.size());
            }
            //获取到获取码商ID
            Long merchantId = weightObjs.next();

            //根据码商ID过滤出码商的二维码
            List<SuitableQrcodeVO> filterMerchantList = list.stream().filter(a -> a.getMerchantId().equals(merchantId)).sorted(Comparator.comparing(SuitableQrcodeVO::getQrcodeId)).collect(Collectors.toList());
            //码商的二维码轮询
            String preKey = RedisKeys.orderPreQrcodeChannel + shopId + ":" + merchantId + ":" + channelId;
            if (redisUtils.hasKey(preKey)) {
                long preQrcodeId = redisUtils.get(preKey, Long.class);
                List<SuitableQrcodeVO> newList = filterMerchantList.stream().filter(a -> a.getQrcodeId() > preQrcodeId).collect(Collectors.toList());
                if (!newList.isEmpty()) {
                    result = newList.get(0);
                }
            }
            redisUtils.set(preKey, result.getQrcodeId());
        }catch (Exception e){
            result = list.get(0);
            String preKey = RedisKeys.orderPreQrcodeChannel + shopId + ":" + result.getMerchantId() + ":" + channelId;
            redisUtils.set(preKey, result.getQrcodeId());
        }
        return result;
*/

        //老方法，直接根据二维码先后顺序进行轮询
        List<SuitableQrcodeVO> result = new ArrayList<>();
        String preKey = RedisKeys.orderPreQrcodeChannel + shopId + ":" + channelId;
        try {
            list = list.stream().sorted(Comparator.comparing(SuitableQrcodeVO::getQrcodeId)).collect(Collectors.toList());
            log.info("轮询前的所有二维码ID{}", JSONUtil.toJsonStr(list.stream().map(SuitableQrcodeVO::getQrcodeId).collect(Collectors.toList())));
            if (redisUtils.hasKey(preKey)) {
                long preQrcodeId = redisUtils.get(preKey, Long.class);
                List<SuitableQrcodeVO> newList = list.stream().filter(a -> a.getQrcodeId() > preQrcodeId).collect(Collectors.toList());
                if (!newList.isEmpty()) {
                    result.add(newList.get(0));
                } else {
                    result.add(list.get(0));
                }
            } else {
                result.add(list.get(0));
            }
        }catch (Exception e){
            log.error("二维码轮询报错:{}",e.getMessage());
            Collections.shuffle(list);
            result.add(list.get(0));
        } finally {
            redisUtils.set(preKey, result.get(0).getQrcodeId());
        }
        log.info("轮询后的二维码ID:{}", result.get(0).getQrcodeId());
        return result.get(0);
    }

    public SuitableQrcodeVO getMerchantByPolling(List<SuitableQrcodeVO> list, Long channelId, String shopId){
        if (list.size() == 1) {
            return list.get(0);
        }
        List<SuitableQrcodeVO> result = new ArrayList<>();
        String preKey = RedisKeys.orderPreMerchantChannel + shopId + ":" + channelId;
        try {
            list = list.stream().sorted(Comparator.comparing(SuitableQrcodeVO::getMerchantId)).collect(Collectors.toList());
            if (redisUtils.hasKey(preKey)) {
                long preMerchantId = redisUtils.get(preKey, Long.class);
                List<SuitableQrcodeVO> newList = list.stream().filter(a -> a.getMerchantId() > preMerchantId).collect(Collectors.toList());
                if (!newList.isEmpty()) {
                    result.add(newList.get(0));
                } else {
                    result.add(list.get(0));
                }
            } else {
                result.add(list.get(0));
            }
//            list = list.stream().sorted(Comparator.comparing(SuitableQrcodeVO::getMerchantId)).collect(Collectors.toList());
//            //码商轮询
//            int maxDrop = list.size() - 1;
//            Long mindex = redisUtils.getNext(RedisKeys.orderMerchantPolling + shopId + ":" + channelId, maxDrop);
//            result.add(list.get(mindex.intValue()));
        }catch (Exception e){
            log.error("二维码轮询报错:{}",e.getMessage());
            Collections.shuffle(list);
            result.add(list.get(0));
        }finally {
            redisUtils.set(preKey, result.get(0).getMerchantId());
        }
        return result.get(0);
    }

    public SuitableQrcodeVO getMerchantQrcodePolling(List<SuitableQrcodeVO> list, SuitableQrcodeVO vo){
        SuitableQrcodeVO result = list.get(0);
        String preKey = RedisKeys.createOrderPreMerQrcode + vo.getMerchantId() + ":" + vo.getChannelId();
        try {
            if (list.size() > 1) {
                if (redisUtils.hasKey(preKey)) {
                    long preQrcodeId = redisUtils.get(preKey, Long.class);
                    List<SuitableQrcodeVO> newList = list.stream().filter(a -> a.getQrcodeId() > preQrcodeId).sorted(Comparator.comparing(SuitableQrcodeVO::getQrcodeId)).collect(Collectors.toList());
                    if (!newList.isEmpty()) {
                        result = newList.get(0);
                    }
                }
//                //码商的二维码轮询
//                int maxDrop = list.size() - 1;
//                Long mindex = redisUtils.getNext(RedisKeys.createOrderMerchant + vo.getMerchantId() + ":" + vo.getChannelId(), maxDrop);
//                lastQrcode = list.get(mindex.intValue());
            }
        }catch (Exception e){
            log.error("二维码轮询报错:{}",e.getMessage());
        } finally {
            redisUtils.set(preKey, result.getQrcodeId());
        }
        return result;
    }

    public AjaxResult query(Map<String,Object> req){
        if (ObjectUtil.isNull(req.get("out_trade_no"))){
            return AjaxResult.error(1,"商户订单号为空");
        }
        if (ObjectUtil.isNull(req.get("mchid"))){
            return AjaxResult.error(1,"商户号为空");
        }
        if (ObjectUtil.isNull(req.get("channel"))){
            return AjaxResult.error(1,"渠道编码为空");
        }
        if (ObjectUtil.isNull(req.get("sign"))){
            return AjaxResult.error(1,"签名为空");
        }
        JSONObject signReq = new JSONObject();
        signReq.set("out_trade_no", req.get("out_trade_no"));
        signReq.set("mchid", req.get("mchid"));
        signReq.set("channel", req.get("channel"));
        Object shopInfo = redisUtils.get(RedisKeys.shopInfo);
        List<ShopEntity> shopList = JSONUtil.toList(shopInfo.toString(), ShopEntity.class);
        String shopId = req.get("mchid").toString();
        ShopEntity shopEntity = shopList.stream().filter(a -> StrUtil.equals(a.getUserId().toString(), shopId)).findFirst().orElse(null);
        if (shopEntity == null) {
            return AjaxResult.error(1, "商户ID有误");
        }
        /*验证签名*/
        Map<String, Object> mapreq = BeanUtil.beanToMap(signReq);
        mapreq.remove("sign");
        String signStr = DESUtil.getSingByMap(mapreq) + "&key=" + shopEntity.getSignSecret();
        String signNew = DigestUtil.md5Hex(signStr);
        if (!StrUtil.equals(signNew.toLowerCase(), req.get("sign").toString())) {
            log.error("签名有误，{}", JSONUtil.toJsonStr(req));
            return AjaxResult.error(1, "签名有误");
        }
        List<InOrderEntity> orderList = orderService.list(Wrappers.lambdaQuery(InOrderEntity.class).eq(InOrderEntity::getShopOrderNo, req.get("out_trade_no")));
        if (orderList == null || orderList.isEmpty()) {
            return AjaxResult.error(1, "订单不存在");
        }
        InOrderEntity order = orderList.get(0);
        JSONObject result = new JSONObject();
        result.set("trade_no", order.getTradeNo());
        result.set("out_trade_no", order.getShopOrderNo());
        result.set("amount", order.getOrderAmount());
        result.set("status", "WAIT");
        if (order.getOrderStatus() == OrderStatus.FINISH.getValue()) {
            result.set("status", "SUCCESS");
        }
        return AjaxResult.success(0,"请求成功",result);
    }

    public String result() {
       String json = "[  {\n" +
               "        \"qrcodeId\": 2435,\n" +
               "        \"merchantId\": 10327,\n" +
               "        \"merchantName\": \"qq88888\",\n" +
               "        \"agentId\": 10053,\n" +
               "        \"agentName\": \"qiezi\",\n" +
               "        \"agentRate\": 0,\n" +
               "        \"nickName\": \"李毓平\",\n" +
               "        \"accountNumber\": \"18377508425\",\n" +
               "        \"accountRemark\": \"\",\n" +
               "        \"qrcodeMinAmount\": 1000,\n" +
               "        \"qrcodeMaxAmount\": 50000,\n" +
               "        \"qrcodeType\": 0,\n" +
               "        \"qrcodeValue\": \"495890\",\n" +
               "        \"qrcodeUrl\": \"\",\n" +
               "        \"dayLimit\": 0,\n" +
               "        \"countLimit\": 0,\n" +
               "        \"merchantRate\": 2.5,\n" +
               "        \"merchantMinAmount\": 1000,\n" +
               "        \"merchantMaxAmount\": 50000,\n" +
               "        \"shopId\": 10240,\n" +
               "        \"shopName\": \"查单总群\",\n" +
               "        \"signSecret\": \"a19e7f406bce4a6b9e4967bf799b9bbca19e7f406bce4a6b\",\n" +
               "        \"shopMinAmount\": 0,\n" +
               "        \"shopMaxAmount\": 0,\n" +
               "        \"shopRate\": 0,\n" +
               "        \"channelId\": 1106,\n" +
               "        \"channelCode\": \"alipayCodeBig\",\n" +
               "        \"channelName\": \"支付宝扫码大额\",\n" +
               "        \"overtime\": 5,\n" +
               "        \"merchantOvertime\": 10,\n" +
               "        \"weight\": 150,\n" +
               "        \"overAmount\": 0,\n" +
               "        \"lockedCode\": 1,\n" +
               "        \"setAmount\": 0,\n" +
               "        \"overAmountValue\": 30,\n" +
               "        \"merchantChannelId\": 1733,\n" +
               "        \"agentChannelId\": 19,\n" +
               "        \"qrcodeModel\": \"alipaycode\",\n" +
               "        \"minMerchantGetAmount\": 100,\n" +
               "        \"orderRemind\": 0,\n" +
               "        \"baseDeposit\": 2000,\n" +
               "        \"weightType\": 1,\n" +
               "        \"merchantRateOne\": 3.5,\n" +
               "        \"merchantRateTwo\": 3.5,\n" +
               "        \"merchantRateThree\": 2.5,\n" +
               "        \"merchantRateFour\": 0,\n" +
               "        \"merchantRateFive\": 0\n" +
               "    },\n" +
               "    {\n" +
               "        \"qrcodeId\": 2476,\n" +
               "        \"merchantId\": 10327,\n" +
               "        \"merchantName\": \"qq88888\",\n" +
               "        \"agentId\": 10053,\n" +
               "        \"agentName\": \"qiezi\",\n" +
               "        \"agentRate\": 0,\n" +
               "        \"nickName\": \"邓考蓓\",\n" +
               "        \"accountNumber\": \"13084358872\",\n" +
               "        \"accountRemark\": \"\",\n" +
               "        \"qrcodeMinAmount\": 1000,\n" +
               "        \"qrcodeMaxAmount\": 50000,\n" +
               "        \"qrcodeType\": 0,\n" +
               "        \"qrcodeValue\": \"102858\",\n" +
               "        \"qrcodeUrl\": \"\",\n" +
               "        \"dayLimit\": 0,\n" +
               "        \"countLimit\": 0,\n" +
               "        \"merchantRate\": 2.5,\n" +
               "        \"merchantMinAmount\": 1000,\n" +
               "        \"merchantMaxAmount\": 50000,\n" +
               "        \"shopId\": 10240,\n" +
               "        \"shopName\": \"查单总群\",\n" +
               "        \"signSecret\": \"a19e7f406bce4a6b9e4967bf799b9bbca19e7f406bce4a6b\",\n" +
               "        \"shopMinAmount\": 0,\n" +
               "        \"shopMaxAmount\": 0,\n" +
               "        \"shopRate\": 0,\n" +
               "        \"channelId\": 1106,\n" +
               "        \"channelCode\": \"alipayCodeBig\",\n" +
               "        \"channelName\": \"支付宝扫码大额\",\n" +
               "        \"overtime\": 5,\n" +
               "        \"merchantOvertime\": 10,\n" +
               "        \"weight\": 150,\n" +
               "        \"overAmount\": 0,\n" +
               "        \"lockedCode\": 1,\n" +
               "        \"setAmount\": 0,\n" +
               "        \"overAmountValue\": 30,\n" +
               "        \"merchantChannelId\": 1733,\n" +
               "        \"agentChannelId\": 19,\n" +
               "        \"qrcodeModel\": \"alipaycode\",\n" +
               "        \"minMerchantGetAmount\": 100,\n" +
               "        \"orderRemind\": 0,\n" +
               "        \"baseDeposit\": 2000,\n" +
               "        \"weightType\": 1,\n" +
               "        \"merchantRateOne\": 3.5,\n" +
               "        \"merchantRateTwo\": 3.5,\n" +
               "        \"merchantRateThree\": 2.5,\n" +
               "        \"merchantRateFour\": 0,\n" +
               "        \"merchantRateFive\": 0\n" +
               "    }]";
       List<SuitableQrcodeVO> list = JSONUtil.toList(json, SuitableQrcodeVO.class);
        //根据码商去重
        List<SuitableQrcodeVO> match7 = list.stream().collect(
                Collectors.collectingAndThen(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator.comparing(SuitableQrcodeVO::getMerchantId))), ArrayList::new));
        System.out.println(JSONUtil.toJsonStr(match7.stream().map(SuitableQrcodeVO::getMerchantId).collect(Collectors.toList())));
        //根据权重选举出码商
        SuitableQrcodeVO match8  = getListByWeightMerchant(match7);
        log.info("码商的所有二维码ID:{}", JSONUtil.toJsonStr(list.stream().map(SuitableQrcodeVO::getQrcodeId).collect(Collectors.toList())));
        //获取该码商的所有码
        list = list.stream().filter(a -> a.getMerchantId().equals(match8.getMerchantId())).sorted(Comparator.comparing(SuitableQrcodeVO::getQrcodeId)).collect(Collectors.toList());
        log.info("根据权重过滤后码商的所有二维码ID:{}", JSONUtil.toJsonStr(list.stream().map(SuitableQrcodeVO::getQrcodeId).collect(Collectors.toList())));
        //码商的二维码轮询
        int maxDrop = list.size() - 1;

        Long mindex = redisUtils.getNext(RedisKeys.createOrderMerchant + match8.getMerchantId()+ ":" + match8.getChannelId(), maxDrop);
        System.out.println(mindex);
        SuitableQrcodeVO lastQrcode = list.get(mindex.intValue());
        return JSONUtil.toJsonStr(lastQrcode.getNickName());
    }

    @Async
    public void sendReceiptImgToTg(InOrderEntity order) {
        try {
            TgOrderInfo queryEntity = new TgOrderInfo();
            queryEntity.setClientOrderNo(order.getShopOrderNo());
            TgOrderInfo orderInfo = orderService.queryTgOrder(queryEntity);
            if (orderInfo != null && StrUtil.isNotEmpty(orderInfo.getPtChatId())) {
                sfTelegramBot.sendPhotoByReceiptImg(orderInfo.getPtChatId(), order.getReceiptImg(),order.getMerchantName() + "  " +  order.getTradeNo() + " 回单");
            } else {
                log.info("会员上传回单，未找到码商群,单号：{}", order.getTradeNo());
            }
        }catch (Exception e) {
            e.printStackTrace();
            log.error("发送回单至码商群失败:{}", e.getMessage());
        }
    }
}
