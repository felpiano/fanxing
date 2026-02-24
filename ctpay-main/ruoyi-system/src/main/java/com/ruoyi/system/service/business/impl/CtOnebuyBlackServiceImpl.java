package com.ruoyi.system.service.business.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.system.domain.business.CtOnebuyBlack;
import com.ruoyi.system.mapper.business.CtOnebuyBlackMapper;
import com.ruoyi.system.service.business.ICtOnebuyBlackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 一码通黑名单Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-02
 */
@Service
public class CtOnebuyBlackServiceImpl  extends ServiceImpl<CtOnebuyBlackMapper, CtOnebuyBlack> implements ICtOnebuyBlackService {
    @Autowired
    private CtOnebuyBlackMapper ctOnebuyBlackMapper;


    @Override
    public boolean checkPayer(String payer) {
        if (StrUtil.isBlank(payer)) {
            return false;
        }
        LambdaQueryWrapper<CtOnebuyBlack> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CtOnebuyBlack::getPayer, payer);
        List<CtOnebuyBlack> list = this.list(wrapper);
        if (CollUtil.isNotEmpty(list)) {
            return true;
        }
        return false;
    }
}
