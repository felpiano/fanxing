package com.ruoyi.system.service.business.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.system.domain.business.MerchantHistoryBalanceEntity;
import com.ruoyi.system.mapper.business.MerchantHistoryBalanceMapper;
import com.ruoyi.system.service.business.MerchantHistoryBalanceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 码商每日余额历史 服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-11-06
 */
@Service
public class MerchantHistoryBalanceServiceImp extends ServiceImpl<MerchantHistoryBalanceMapper, MerchantHistoryBalanceEntity> implements MerchantHistoryBalanceService {

    @Override
    public void init(String date) {
        List<MerchantHistoryBalanceEntity> saveList = new ArrayList<>();
        for (int i=1;i<=9;i++) {
            String bdate = DateUtil.format(DateUtil.offsetDay(DateUtil.parse(date, "yyyy-MM-dd"), -1 * i), "yyyy-MM-dd");
            String aDate = DateUtil.format(DateUtil.offsetDay(DateUtil.parse(date, "yyyy-MM-dd"), -1 * i), "yyyy-MM-dd 23:59:59");
            List<MerchantHistoryBalanceEntity> list = baseMapper.init(aDate, bdate);
            if (list != null && !list.isEmpty()) {
                list.forEach(data -> {
                    data.setDataDt(bdate);
                });
                saveList.addAll(list);
            }
        }
        this.saveBatch(saveList);
    }

    @Override
    public void everyDayTask() {
        String date = DateUtil.format(DateUtil.offsetDay(new Date(), -1), "yyyy-MM-dd");
        String aDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1), "yyyy-MM-dd 23:59:59");
        List<MerchantHistoryBalanceEntity> list = baseMapper.init(aDate, date);
        if (list != null && !list.isEmpty()) {
            list.forEach(data -> {
                data.setDataDt(date);
            });
            this.saveBatch(list);
        }
        //删除30天前的数据
        this.remove(Wrappers.lambdaQuery(MerchantHistoryBalanceEntity.class)
                .eq(MerchantHistoryBalanceEntity::getDataDt, DateUtil.format(DateUtil.offsetDay(new Date(), -30), "yyyy-MM-dd")));
    }
}
