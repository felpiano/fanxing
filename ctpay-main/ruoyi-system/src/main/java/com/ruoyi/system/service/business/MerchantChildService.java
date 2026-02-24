package com.ruoyi.system.service.business;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.dto.MerchantChildSaveDTO;

import java.math.BigDecimal;

public interface MerchantChildService {
    /**
     * 添加下级
     * @param loginName
     * @param password
     * @return
     */
    AjaxResult addChildMerchant(String loginName, String nickName, String password);

    /**
     * 转移余额
     * @param childId
     * @param changeAmount
     * @return
     */
    AjaxResult trimBalance(Long childId, BigDecimal changeAmount,String remark);

    AjaxResult updateChildMerchant(MerchantChildSaveDTO saveDTO);

    /**
     * 自由转移金额
     * @param toId
     * @param changeAmount
     * @param remark
     */
    AjaxResult trimBalanceFree(Long toId, BigDecimal changeAmount,String remark);
}
