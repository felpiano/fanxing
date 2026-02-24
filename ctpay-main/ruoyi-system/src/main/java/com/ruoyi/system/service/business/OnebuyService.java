package com.ruoyi.system.service.business;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.dto.OnebuyCreateOrderRes;
import com.ruoyi.system.domain.dto.OnebuyOrderReq;

import java.util.List;

public interface OnebuyService {
    void heartbeat(List<String> shopIdList);

    void online(List<String> shopIdList);

    void offline(List<String> shopIdList);

    void orderReport(List<OnebuyOrderReq> list);

    /**
     *
     * @param orderNo  订单号
     * @param amount   金额（分）
     * @param shopIdList
     * @return
     */
    OnebuyCreateOrderRes createOnebuyOrder(String orderNo, String amount, List<String> shopIdList) throws ServiceException;
}
