package com.ruoyi.web.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.RedisLock;
import com.ruoyi.system.domain.business.*;
import com.ruoyi.system.domain.vo.MerchantDepositVO;
import com.ruoyi.system.service.business.*;
import com.ruoyi.system.telegram.tgutil.TgUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class BaseSyncTask {
    @Resource
    private AgentService agentService;
    @Resource
    private ChannelService channelService;
    @Resource
    private MerchantQrcodeService merchantQrcodeService;
    @Resource
    private AgentChannelService agentChannelService;
    @Resource
    private ShopService shopService;
    @Resource
    private ShopBaseChannelService shopBaseChannelService;
    @Resource
    private ShopMerchantRelationService shopMerchantRelationService;
    @Resource
    private MerchantService merchantService;
    @Resource
    private MerchantChannelService merchantChannelService;
    @Resource
    private RedisLock redisLock;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private InOrderService inOrderService;
    @Resource
    private InOrderDetailService inOrderDetailService;

    /**
     * 同步代理产品、码商的单码信息至redis缓存
     */
    @Scheduled(cron = "*/2 * * * * ?")
    public void qrcodeDayLimitInit(){
        try {
            boolean flag = redisLock.getLock(RedisKeys.bashinfoUpdateLock, "1");
            if (flag) {
                //通道
                List<ChannelEntity> channelList = channelService.list();
                /*if (channelList != null) {
                    redisUtils.set(RedisKeys.channelInfo, JSONUtil.toJsonStr(channelList), -1);
                }*/
                //代理产品
                List<AgentChannelEntity> agentChannelList = agentChannelService.list();
                /*if (agentChannelList != null) {
                    redisUtils.set(RedisKeys.agentChannel, JSONUtil.toJsonStr(agentChannelList), -1);
                }*/
                //码商的码
                List<MerchantQrcodeEntity> merchantQrcodeList = merchantQrcodeService.list();
                /* if (merchantQrcodeList != null) {
                    redisUtils.set(RedisKeys.merchantQrcode, JSONUtil.toJsonStr(merchantQrcodeList), -1);
                }*/
                //商户
                List<ShopEntity> shopList = shopService.list();
                /*if (shopList != null) {
                    redisUtils.set(RedisKeys.shopInfo, JSONUtil.toJsonStr(shopList), -1);
                }*/
                //商户通道
                List<ShopBaseChannelEntity> shopChannelList = shopBaseChannelService.list();
                /*if (shopChannelList != null) {
                    redisUtils.set(RedisKeys.shopChannelInfo, JSONUtil.toJsonStr(shopChannelList), -1);
                }*/
                //商户码商
                List<ShopMerchantRelationEntity> merchantRelationList = shopMerchantRelationService.list();
                /*if (merchantRelationList != null) {
                    redisUtils.set(RedisKeys.shopMerchantInfo, JSONUtil.toJsonStr(merchantRelationList), -1);
                }*/
                //码商
                List<MerchantEntity> merchantList = merchantService.list();
                /*if (merchantList != null) {
                    redisUtils.set(RedisKeys.merchantInfo, JSONUtil.toJsonStr(merchantList), -1);
                }*/
                //码商通道
                List<MerchantChannelEntity> merchantChannelList = merchantChannelService.list();
                /*if (merchantChannelList != null) {
                    redisUtils.set(RedisKeys.merchantChannelInfo, JSONUtil.toJsonStr(merchantChannelList), -1);
                }*/

                List<MerchantDepositVO> merchantDeposits = merchantService.listBaseDeposit();
                /*if (merchantDeposits != null) {
                    redisUtils.set(RedisKeys.merchantDeposit, JSONUtil.toJsonStr(merchantDeposits), -1);
                }*/

                //代理
                List<AgentEntity> agentList = agentService.list();
                redisTemplate.execute(new SessionCallback<Object>() {
                    @Override
                    public Object execute(RedisOperations operations) {
                        operations.multi();
                        operations.opsForValue().set(RedisKeys.channelInfo, JSONUtil.toJsonStr(channelList));
                        operations.opsForValue().set(RedisKeys.agentChannel, JSONUtil.toJsonStr(agentChannelList));
                        operations.opsForValue().set(RedisKeys.merchantQrcode, JSONUtil.toJsonStr(merchantQrcodeList));
                        operations.opsForValue().set(RedisKeys.shopInfo, JSONUtil.toJsonStr(shopList));
                        operations.opsForValue().set(RedisKeys.shopChannelInfo, JSONUtil.toJsonStr(shopChannelList));
                        operations.opsForValue().set(RedisKeys.shopMerchantInfo, JSONUtil.toJsonStr(merchantRelationList));
                        operations.opsForValue().set(RedisKeys.merchantInfo, JSONUtil.toJsonStr(merchantList));
                        operations.opsForValue().set(RedisKeys.merchantChannelInfo, JSONUtil.toJsonStr(merchantChannelList));
                        operations.opsForValue().set(RedisKeys.merchantDeposit, JSONUtil.toJsonStr(merchantDeposits));
                        operations.opsForValue().set(RedisKeys.agentInfo, JSONUtil.toJsonStr(agentList));
                        return operations.exec();
                    }
                });
                //Telegram 信息同步
                TgUserUtil.setTgUserMap(shopList,merchantList);
            }
        }catch (Exception e){

        }finally {
            redisLock.releaseLock(RedisKeys.bashinfoUpdateLock, "1");
        }
    }

    //删除180天前的订单数据
/*    @Scheduled(cron = "0 0 0 * * ?")
    private void deleteOrders() {
        String endTime = DateUtil.format(DateUtil.offsetDay(new Date(), -180), "yyyy-MM-dd 00:00:00");
        log.info("删除{}前的订单数据", endTime);
        inOrderService.remove(Wrappers.lambdaQuery(InOrderEntity.class)
                .lt(InOrderEntity::getOrderTime, endTime));
        inOrderDetailService.remove(Wrappers.lambdaQuery(InOrderDetailEntity.class)
                .lt(InOrderDetailEntity::getDetailTime, endTime));

    }*/
}
