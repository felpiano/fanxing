package com.ruoyi.system.service.business;

import com.ruoyi.system.domain.business.ReqOrderEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.dto.ShopOrderReq;

/**
 * <p>
 * 订单请求 服务类
 * </p>
 *
 * @author admin
 * @since 2024-11-18
 */
public interface ReqOrderService extends IService<ReqOrderEntity> {
    void addReqOrder(ShopOrderReq reqOrder);
}
