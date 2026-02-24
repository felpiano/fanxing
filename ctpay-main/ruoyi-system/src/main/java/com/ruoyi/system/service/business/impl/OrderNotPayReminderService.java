package com.ruoyi.system.service.business.impl;

import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.system.domain.business.InOrderEntity;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.business.MerchantService;
import com.ruoyi.system.telegram.SFTelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Service
@Slf4j
public class OrderNotPayReminderService {

    @Resource
    private SFTelegramBot kmsfTelegramBot;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private ISysUserService userService;
    @Resource
    private MerchantService merchantService;

    @Async
    public void orderNotPayReminder(InOrderEntity order, int type) {
        try {
            MerchantEntity merchant = merchantService.getById(order.getMerchantId());
            if (merchant != null && merchant.getAgentId() == 10053) {
                String redisKey = RedisKeys.merchantChannelOrderNotPay + order.getMerchantId() + ":" + order.getChannelId();
                if (type == 0) {
                    redisUtils.incr(redisKey, 1);
                    Object failCount = redisUtils.get(redisKey);
                    if (ObjectUtil.isNotEmpty(failCount) && new BigDecimal(failCount.toString()).compareTo(new BigDecimal("5")) >= 0) {
                        String message = "码商" + merchant.getMerchantName() + "(" + merchant.getUserName() + "-" + order.getMerchantId() + ")的" + order.getChannelName() + "通道，已连续" + failCount + "笔未确认收款，请关注";
                        SysUser sysUser = userService.selectUserById(1L);
                        String botList = sysUser.getBotList();
                        if (botList != null && !botList.isEmpty()) {
                            String[] bots = botList.split(";");
                            for (String bot : bots) {
                                kmsfTelegramBot.sendReply(Long.parseLong(bot), message);
                            }
                        }
                    }
                } else {
                    redisUtils.delete(redisKey);
                }
            }
        }catch (Exception e){
            log.error("码商通道连续笔数处理异常");
            e.printStackTrace();
        }
    }
}
