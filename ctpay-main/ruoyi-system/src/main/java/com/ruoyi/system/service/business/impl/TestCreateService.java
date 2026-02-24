package com.ruoyi.system.service.business.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.constant.ChannelConstants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.enums.BusinessConfigKey;
import com.ruoyi.common.enums.OrderStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DESUtil;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.system.domain.business.InOrderEntity;
import com.ruoyi.system.domain.dto.OnebuyCreateOrderRes;
import com.ruoyi.system.domain.dto.ShopOrderTestReq;
import com.ruoyi.system.domain.vo.SuitableQrcodeVO;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.business.ICtOnebuyBlackService;
import com.ruoyi.system.service.business.InOrderService;
import com.ruoyi.system.service.business.OnebuyService;
import com.ruoyi.system.service.impl.SnowflakeIdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TestCreateService {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private SnowflakeIdService snowflakeIdService;

    @Resource
    private InOrderService orderService;

    @Resource
    private ISysConfigService sysConfigService;
    @Autowired
    private ShopOrderService shopOrderService;
    @Autowired
    private OnebuyService onebuyService;
    @Autowired
    private ICtOnebuyBlackService iCtOnebuyBlackService;

    public AjaxResult createOrder(ShopOrderTestReq req, String clientIp){
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
            return AjaxResult.error(1,"没有可用的二维码");
        }
        String json = redisUtils.get(RedisKeys.suitableQrcodeList);
        if (StrUtil.isEmpty(json)) {
            return AjaxResult.error(1,"没有可用的二维码");
        }
        List<SuitableQrcodeVO> qrCodeList = JSONUtil.toList(json, SuitableQrcodeVO.class);
        if (qrCodeList == null || qrCodeList.isEmpty()) {
            return AjaxResult.error(1,"没有可用的二维码");
        }

        //匹配码商
        List<SuitableQrcodeVO> merchantMatchList = qrCodeList.stream().filter(code ->
                StrUtil.equals(code.getMerchantId().toString(), req.getMerchantId())
        ).collect(Collectors.toList());
        if (merchantMatchList.isEmpty()) {
            log.error("码商ID错误，{}", JSONUtil.toJsonStr(req));
            throw new ServiceException("选择的商户没有合适的码");
        }

        //根据商户ID匹配
        List<SuitableQrcodeVO> shopMatchList = merchantMatchList.stream().filter(code ->
                StrUtil.equals(code.getShopId().toString(), req.getMchid())
        ).collect(Collectors.toList());
        if (shopMatchList.isEmpty()) {
            log.error("商户ID错误，{}", JSONUtil.toJsonStr(req));
            throw new ServiceException("选择的码商没有合适的码");
        }
        /*验证签名*/
        String shopSignSecret = shopMatchList.get(0).getSignSecret();
        Map<String, Object> mapreq = BeanUtil.beanToMap(req);
        mapreq.remove("sign");
        mapreq.remove("merchantId");
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
            throw new ServiceException("选择的支付通道没有合适码");
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
            if (shopOrderService.matchAmount(req.getAmount(), m1.getShopMinAmount(), m1.getShopMaxAmount())) {
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
            if (shopOrderService.matchAmount(req.getAmount(), m2.getMerchantMinAmount(), m2.getMerchantMaxAmount())) {
                match3.add(m2);
            }
        }
        if (match3.isEmpty()) {
            log.error("下单金额超出码商设置的范围，{}", JSONUtil.toJsonStr(req));
            List<SuitableQrcodeVO> oneMerchant = match2.stream().collect(
                    Collectors.collectingAndThen(Collectors.toCollection(() ->
                            new TreeSet<>(Comparator.comparing(SuitableQrcodeVO::getMerchantName))), ArrayList::new));
            String merchantNames = oneMerchant.stream().map(SuitableQrcodeVO::getMerchantName).collect(Collectors.joining(","));
            throw new ServiceException("下单金额超出码商["+merchantNames+"]可接单金额的范围");
        }

        //根据码商余额及最小接单金额过滤
        List<SuitableQrcodeVO> match4 = new ArrayList<>();
        match3.forEach(m3 -> {
            if (ObjectUtil.isNull(m3.getMinMerchantGetAmount()) || new BigDecimal(req.getAmount()).compareTo(m3.getMinMerchantGetAmount()) >= 0) {
                BigDecimal merchantBalance = new BigDecimal(redisUtils.get(RedisKeys.merchantBalance + m3.getMerchantId()));
                BigDecimal yj = ObjectUtil.isNotEmpty(m3.getBaseDeposit())?m3.getBaseDeposit():BigDecimal.ZERO;
                if (merchantBalance.subtract(yj).compareTo(new BigDecimal(req.getAmount())) >= 0) {
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
            throw new ServiceException("码商["+merchantNames+"]余额不足");
        }

        //根据下单金额匹配单码限制
        List<SuitableQrcodeVO> match5 = new ArrayList<>();
        if (match4.get(0).getSetAmount() == 0) {
            for (SuitableQrcodeVO m4 : match4) {
                if (shopOrderService.matchAmount(req.getAmount(), m4.getQrcodeMinAmount(), m4.getQrcodeMaxAmount())) {
                    match5.add(m4);
                }
            }
        } else {
            match5.addAll(match4);
        }
        if (match5.isEmpty()) {
            log.error("下单金额超出二维码设置的范围，{}", JSONUtil.toJsonStr(req));
            List<SuitableQrcodeVO> oneMerchant = match4.stream().collect(
                    Collectors.collectingAndThen(Collectors.toCollection(() ->
                            new TreeSet<>(Comparator.comparing(SuitableQrcodeVO::getMerchantName))), ArrayList::new));
            String merchantNames = oneMerchant.stream().map(SuitableQrcodeVO::getMerchantName).collect(Collectors.joining(","));
            throw new ServiceException("下单金额超出码商["+merchantNames+"]二维码设置的范围");
        }

        //根据单码配置的日限额和笔数限制匹配
        List<SuitableQrcodeVO> match6 = new ArrayList<>();
        match5.forEach(qrcode -> {
            if (qrcode.getDayLimit().compareTo(BigDecimal.ZERO) == 0 && qrcode.getCountLimit() == 0) {
                match6.add(qrcode);
            } else if (qrcode.getCountLimit() != 0){
                String oneKeys = RedisKeys.merchantQrcodeLimitCount + qrcode.getQrcodeId();
                if (!redisUtils.hasKey(oneKeys)) {
                    redisUtils.set(oneKeys, "0");
                }
                String nowLimit = redisUtils.get(oneKeys);
                if ((new BigDecimal(nowLimit).add(new BigDecimal(1))).compareTo(new BigDecimal(qrcode.getCountLimit())) <= 0) {
                    match6.add(qrcode);
                }
            } else if (qrcode.getDayLimit().compareTo(BigDecimal.ZERO) != 0) {
                String oneKeys = RedisKeys.merchantQrcodeLimitAmount + qrcode.getQrcodeId();
                if (!redisUtils.hasKey(oneKeys)) {
                    redisUtils.set(oneKeys, "0");
                }
                String nowLimit = redisUtils.get(oneKeys);
                if (new BigDecimal(nowLimit).add(new BigDecimal(req.getAmount())).compareTo(qrcode.getDayLimit()) <= 0) {
                    match6.add(qrcode);
                }
            }
        });
        if (match6.isEmpty()) {
            log.error("下单金额超出码的日限额限制，请求参数：{}", JSONUtil.toJsonStr(req));
            List<SuitableQrcodeVO> oneMerchant = match5.stream().collect(
                    Collectors.collectingAndThen(Collectors.toCollection(() ->
                            new TreeSet<>(Comparator.comparing(SuitableQrcodeVO::getMerchantName))), ArrayList::new));
            String merchantNames = oneMerchant.stream().map(SuitableQrcodeVO::getMerchantName).collect(Collectors.joining(","));
            throw new ServiceException("下单金额超出码商["+merchantNames+"]所有可进单码的日限额限制");
        }

        //根据权重获取数据
        List<SuitableQrcodeVO> list = new ArrayList<>(match6);
        SuitableQrcodeVO lastQrcode = match6.get(0);

        //根据权重获取数据
//        Integer weightType = match6.get(0).getWeightType();
//        if (weightType != null && weightType == 0) {
//            list = shopOrderService.getListByWeight(match6);
//            lastQrcode = list.get(0);
//        } else {
//            //根据码商去重
            List<SuitableQrcodeVO> match7 = match6.stream().collect(
                    Collectors.collectingAndThen(Collectors.toCollection(() ->
                            new TreeSet<>(Comparator.comparing(SuitableQrcodeVO::getMerchantId))), ArrayList::new));
            //根据权重选举出码商
            SuitableQrcodeVO match8 = shopOrderService.getListByWeightMerchant(match7);
            //获取该码商的所有码
            list = list.stream().filter(a -> a.getMerchantId().equals(match8.getMerchantId())).collect(Collectors.toList());
            Collections.shuffle(list);
            lastQrcode = list.get(0);
//        }


        BigDecimal fixedAmount = new BigDecimal(req.getAmount());
        //判断是否开启锁码
        if (lastQrcode.getLockedCode() == 0 && lastQrcode.getOverAmount() != 0) {
            lastQrcode = shopOrderService.getLastQrcode(list, lastQrcode);
        } else if (lastQrcode.getLockedCode() == 0) {
            lastQrcode = shopOrderService.getLastQrcodeLast(list, fixedAmount, lastQrcode);
            fixedAmount = lastQrcode.getFixAmount();
        } else if (lastQrcode.getOverAmount() == 0){
            lastQrcode = shopOrderService.getLastQrcodeAmount(list, fixedAmount, lastQrcode);
            fixedAmount = lastQrcode.getFixAmount();
        }

        if (lastQrcode == null || ObjectUtil.isNull(lastQrcode.getQrcodeId())) {
            match6.remove(lastQrcode);
            lastQrcode = list.get(0);
            fixedAmount = new BigDecimal(req.getAmount());
            if (lastQrcode.getLockedCode() == 0 && lastQrcode.getOverAmount() != 0) {
                lastQrcode = shopOrderService.getLastQrcode(match6, lastQrcode);
            } else if (lastQrcode.getLockedCode() == 0) {
                lastQrcode = shopOrderService.getLastQrcodeLast(match6, fixedAmount, lastQrcode);
                fixedAmount = lastQrcode.getFixAmount();
            } else if (lastQrcode.getOverAmount() == 0){
                lastQrcode = shopOrderService.getLastQrcodeAmount(match6, fixedAmount, lastQrcode);
                fixedAmount = lastQrcode.getFixAmount();
            }
        }
        if (lastQrcode == null || ObjectUtil.isNull(lastQrcode.getQrcodeId())) {
            log.info("所有码都已超过浮动或被锁码");
            String merchantNames = match6.stream().map(SuitableQrcodeVO::getMerchantName).collect(Collectors.joining(","));
            throw new ServiceException("所有码商["+merchantNames+"]的可进单码都已超过浮动或被锁码");
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
                .orderStatus(OrderStatus.WAIT.getValue())
                .callbackStatus(0)
                .clientIp(clientIp)
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

}
