package com.ruoyi.web.task;

import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

@Component
@Slf4j
public class MerchantNotPayTask {
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private RedisLock redisLock;
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 清除当日的未收款通道
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void orderNotPayReminder(){
        try {
            if (redisLock.getLock(RedisKeys.merchantChannelTaskLock, "1")) {
                Set<Object> keys = redisUtils.keys(RedisKeys.merchantChannelOrderNotPay + "*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.execute(new SessionCallback<Object>() {
                        @Override
                        public Object execute(RedisOperations operations) {
                            operations.multi();
                            for (Object key : keys) {
                                operations.delete(key);
                            }
                            return operations.exec();
                        }
                    });
                }
            }
        }catch (Exception e){
            log.error("清除当日的未收款通道出错:{}",e.getMessage());
        }finally {
            redisLock.releaseLock(RedisKeys.merchantChannelTaskLock, "1");
        }
    }
}
