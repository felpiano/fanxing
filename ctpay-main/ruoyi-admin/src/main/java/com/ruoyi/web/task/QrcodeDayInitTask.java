package com.ruoyi.web.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.RedisLock;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.ruoyi.system.domain.business.MerchantHistoryBalanceEntity;
import com.ruoyi.system.domain.business.MerchantQrcodeEntity;
import com.ruoyi.system.domain.vo.SuitableQrcodeVO;
import com.ruoyi.system.service.business.InOrderService;
import com.ruoyi.system.service.business.MerchantHistoryBalanceService;
import com.ruoyi.system.service.business.MerchantQrcodeService;
import com.ruoyi.system.service.business.MerchantService;
import com.ruoyi.system.service.business.impl.AsyncRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class QrcodeDayInitTask {
    @Resource
    private MerchantQrcodeService merchantQrcodeService;
    @Resource
    private AsyncRedisService asyncReportQrcode;
    @Resource
    private RedisLock redisLock;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RedisUtils redisUtils;

    /**
     * 每日零点初始化单码当日接单金额
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void qrcodeDayLimitInit(){
        try {
            boolean flag = redisLock.getLock(RedisKeys.merchantCurrDayLimitLock, "1");
            if (flag) {
                List<MerchantQrcodeEntity> list = merchantQrcodeService.list();
                if (list != null && !list.isEmpty()) {
                    redisTemplate.execute(new SessionCallback<Object>() {
                        @Override
                        public Object execute(RedisOperations operations) {
                            operations.multi();
                            list.forEach(merchantQrcodeEntity -> {
                                operations.opsForValue().set(RedisKeys.merchantQrcodeLimitAmount + merchantQrcodeEntity.getId(), "0");
                                operations.opsForValue().set(RedisKeys.merchantQrcodeLimitCount + merchantQrcodeEntity.getId(), "0");
                            });
                            return operations.exec();
                        }
                    });
                    merchantQrcodeService.updateBatchById(list);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            redisLock.releaseLock(RedisKeys.merchantCurrDayLimitLock, "1");
        }
    }

    /**
     * 每20秒同步符合状态的单码
     */
    @Scheduled(cron = "0/20 * * * * ?")
    public void suitableQrcode(){
        asyncReportQrcode.asyncReportQrcodeTask();
    }
}
