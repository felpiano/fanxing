package com.ruoyi.system.service.business.impl;

import cn.hutool.json.JSONUtil;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.RedisLock;
import com.ruoyi.system.domain.vo.SuitableQrcodeVO;
import com.ruoyi.system.mapper.business.InOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class AsyncRedisService {
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private InOrderMapper inOrderMapper;
    @Resource
    private RedisLock redisLock;

    @Async
    public void asyncReportQrcode(){
       /* try {
            boolean flag = redisLock.getRetryLock(RedisKeys.merchantQrcodeUpdateLock, "1");
            if (flag) {
                List<SuitableQrcodeVO> list = inOrderMapper.taskByCreateOrder();
                if (list != null && !list.isEmpty()) {
                    redisUtils.set(RedisKeys.suitableQrcodeList, JSONUtil.toJsonStr(list));
                } else {
                    redisUtils.delete(RedisKeys.suitableQrcodeList);
                }
            } else {
                log.error("同步下单码已被锁");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            redisLock.releaseLock(RedisKeys.merchantQrcodeUpdateLock, "1");
        }*/
    }

    public void asyncReportQrcodeTask(){
        try {
            boolean flag = redisLock.getLock(RedisKeys.merchantQrcodeUpdateLock, "1");
            if (flag) {
                List<SuitableQrcodeVO> list = inOrderMapper.taskByCreateOrder();
                if (list != null && !list.isEmpty()) {
                    redisUtils.set(RedisKeys.suitableQrcodeList, JSONUtil.toJsonStr(list));
                } else {
                    redisUtils.delete(RedisKeys.suitableQrcodeList);
                }
            } else {
                log.error("同步下单码已被锁");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            redisLock.releaseLock(RedisKeys.merchantQrcodeUpdateLock, "1");
        }
    }
}
