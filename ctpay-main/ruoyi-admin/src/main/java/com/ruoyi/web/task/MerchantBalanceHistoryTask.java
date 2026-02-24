package com.ruoyi.web.task;

import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.RedisLock;
import com.ruoyi.system.service.business.MerchantHistoryBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class MerchantBalanceHistoryTask {

    @Resource
    private MerchantHistoryBalanceService merchantHistoryBalanceService;
    @Resource
    private RedisLock redisLock;

    @Scheduled(cron = "0 1,20 0 * * ?")
    public void merchantBalanceHistoryTask(){
        try {
            if (redisLock.getLock(RedisKeys.merchantHistoryBalanceTaskLock, "1")) {
                merchantHistoryBalanceService.everyDayTask();
            }
        }catch (Exception e){
            log.error("每日定时任务存储用户昨日余额报错：{}", e.getMessage());
        }finally {
            redisLock.releaseLock(RedisKeys.merchantHistoryBalanceTaskLock, "1");
        }
    }
}
