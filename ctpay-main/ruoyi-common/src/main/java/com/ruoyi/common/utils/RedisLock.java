package com.ruoyi.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;

/**
 * @Date 2024/7/22 22:10
 */
@Component
@Slf4j
public class RedisLock {
    @Autowired
    private RedisTemplate redisTemplate;

    //分布式锁过期时间 s
    private static final Long LOCK_REDIS_TIMEOUT = 60L;
    //分布式锁休眠 至 再次尝试获取 的等待时间 ms 可以根据业务自己调节
    public static final Long LOCK_REDIS_WAIT = 40L;

    /**
     *  加锁
     **/
    public Boolean getLock(String key,String value){
        Boolean lockStatus = this.redisTemplate.opsForValue().setIfAbsent(key,value, Duration.ofSeconds(LOCK_REDIS_TIMEOUT));
        return lockStatus;
    }

    public Boolean getRetryLock(String key,String value){
        Boolean lockStatus = this.redisTemplate.opsForValue().setIfAbsent(key,value, Duration.ofSeconds(LOCK_REDIS_TIMEOUT));
        if (lockStatus) {
            return lockStatus;
        }
        try {
            while (true) {
                Thread.sleep(LOCK_REDIS_WAIT);
                lockStatus = this.redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(LOCK_REDIS_TIMEOUT));
                if (lockStatus) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return lockStatus;
    }

    /**
     *  释放锁
     **/
    public Long releaseLock(String key,String value){
        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        RedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript,Long.class);
        Long releaseStatus = (Long)this.redisTemplate.execute(redisScript, Collections.singletonList(key),value);
        return releaseStatus;
    }
}
