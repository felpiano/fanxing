package com.ruoyi.common.utils;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author admin
 * @Date 2024/9/15 0:24
 */
@Data
@ApiModel("缓存键")
public class RedisKeys {
    /**代理余额*/
    public static final String agentBalance = "agent:balance:";
    /**商户余额*/
    public static final String shopBalance = "shop:balance:";
    /**码商余额锁*/
    public static final String merchantBalanceLocked = "merchant:balance:merchantBalanceLocked";
    /**码商余额*/
    public static final String merchantBalance = "merchant:balance:";
    /**码商预付金额*/
    public static final String merchantPrepayment = "merchant:prepayment:";
    /**码商佣金*/
    public static final String merchantCommission = "merchant:commission:";
    /**usdt价格任务*/
    public static final String usdtPriceTask = "usdt:price:task";
    /**usdt价格信息*/
    public static final String usdtPriceInfo = "usdt:price:info";
    /**码商单码接单笔数*/
    public static final String merchantQrcodeLimitCount = "merchant:qrcode:limitCount:";
    /**码商单码接单金额*/
    public static final String merchantQrcodeLimitAmount = "merchant:qrcode:limitAmount:";
    /**订单超时key**/
    public static final String CPAY_ORDER_TIMEOUT = "fcas:order:timeout:";
    /**订单关闭key**/
    public static final String ORDER_BACK_WAIT = "order:back:wait:";
    /**订单操作 锁**/
    public static final String CPAY_ORDER_OP_LOCK = "fcas:order:op:lock:";
    /**符合状态判断的单码*/
    public static final String suitableQrcodeList = "suitable:qrcode";
    /**码商是否需要锁码*/
    public static final String lockedQrcodeLock = "locked:qrcode:lock:";
    /**码商是否需要锁码*/
    public static final String lockedQrcode = "locked:qrcode:";
    /**码商步加4位数*/
    public static final String merchantUidLock = "merchant:uid:lock:";
    /**商户单码*/
    public static final String merchantQrcode = "merchant:qrcode:list";
    /**产品设置*/
    public static final String agentChannel = "agent:channel:list";
    /**来单提醒*/
    public static final String orderRemind = "order:remind:list";
    /**代理*/
    public static final String agentInfo = "agent:info:list";
    /**通道*/
    public static final String channelInfo = "channel:info:list";
    /**商户信息*/
    public static final String shopInfo = "shop:info:list";
    /**商户通道信息*/
    public static final String shopChannelInfo = "shop:channel:list";
    /**商户码商关联关系*/
    public static final String shopMerchantInfo = "shop:merchant:list";
    /**码商信息*/
    public static final String merchantInfo = "merchant:info:list";
    /**码商信息*/
    public static final String merchantChannelInfo = "merchant:channel:list";
    /**码商通道押金阈值*/
    public static final String merchantDeposit = "merchant:deposit:list";
    /**黑名单*/
    public static final String clientIpList = "clientIp:list:";
    /**码商历史余额定时任务锁*/
    public static final String merchantHistoryBalanceTaskLock = "merchant:historyBalanceTask:lock";
    /**码每日限制更新定时任务锁*/
    public static final String merchantCurrDayLimitLock = "merchant:currDay:limitLock";
    /**码每小时更新合适的码定时任务锁*/
    public static final String merchantQrcodeUpdateLock = "merchant:qrcode:updateLock";
    /**码每小时更新合适的码定时任务锁*/
    public static final String bashinfoUpdateLock = "bashinfo:updateLock";
    /**码商的码轮询*/
    public static final String createOrderMerchant = "create:order:merchant:";
    /**码商的码轮询优化*/
    public static final String createOrderPreMerQrcode = "create:order:preMerchant:qrcode:";
    /**验证码*/
    public static final String validCode = "validCode:";
    /**码商轮询*/
    public static final String orderMerchantPolling = "order:merchant:polling:";
    /**码商轮询优化，上一次的码商ID*/
    public static final String orderPreMerchantChannel = "order:preMerchant:channel:";
    /**二维码轮询*/
    public static final String orderQrcodePolling = "order:qrcode:polling:";
    /**二维码轮询优化，上一次的码商通道ID*/
    public static final String orderPreQrcodeChannel = "order:preQrcode:channel:";
    /**判断同一个码商同一个通道连续5笔订单未支付*/
    public static final String merchantChannelOrderNotPay = "merchant:channel:orderNotPay:";
    /**判断同一个码商同一个通道连续5笔订单未支付定时任务锁*/
    public static final String merchantChannelTaskLock = "merchant:channel:task:lock";

    /**预警*/
    public static final String merchantChannelLess = "merchant:channel:less:";
}
