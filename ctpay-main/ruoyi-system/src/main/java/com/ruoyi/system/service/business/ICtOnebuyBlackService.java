package com.ruoyi.system.service.business;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.business.CtOnebuyBlack;

/**
 * 一码通黑名单Service接口
 * 
 * @author ruoyi
 * @date 2025-12-02
 */
public interface ICtOnebuyBlackService extends IService<CtOnebuyBlack> {

    /**
     * 检查是否存在
     * @param payer
     * @return
     */
    boolean checkPayer(String payer);
}
