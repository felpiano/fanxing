package com.ruoyi.system.service.business;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.business.InOrderDetailEntity;
import com.ruoyi.system.domain.business.InOrderEntity;
import com.ruoyi.system.domain.dto.OrderQueryDTO;
import com.ruoyi.system.domain.vo.MerchantOrderTotalVO;
import com.ruoyi.system.domain.vo.SuitableQrcodeVO;
import com.ruoyi.system.domain.vo.SytQueryVO;
import com.ruoyi.system.telegram.tgDataEntity.TgOrderInfo;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author admin
 * @since 2024-10-20
 */
public interface InOrderService extends IService<InOrderEntity> {

    List<SuitableQrcodeVO> taskByCreateOrder();

    Boolean saveOrder(InOrderEntity order, SuitableQrcodeVO lastQrcode);

    /**
     * 回调处理
     * @return
     */
    AjaxResult repair(InOrderEntity order, InOrderDetailEntity detailEntity);

    /**
     * 自动回调处理
     * @return
     */
    AjaxResult autoRepair(InOrderEntity order, InOrderDetailEntity detailEntity, String operatorMsg);



    /**
     * 未收到付款
     * @param order
     * @param detailEntity
     * @return
     */
    AjaxResult unPaid(InOrderEntity order, InOrderDetailEntity detailEntity);

    /**
     * 收银台查询信息
     * @param tradeNo
     * @return
     */
    R<SytQueryVO> getSytByTradeNo(String tradeNo);

    /**
     * 设置订单支付人
     * @param tradeNo
     * @param payer
     * @return
     */
    AjaxResult setOrderPayer(String tradeNo, String payer);

    /**
     * 码商订单统计
     * @param dto
     * @return
     */
    MerchantOrderTotalVO merchantOrderTotal(OrderQueryDTO dto);

    /**
     * 限额处理
     * @param qrcodeId
     */
    void qrcodeLimitAmountAndCount(Long qrcodeId, BigDecimal amount, Integer type);

    TgOrderInfo queryTgOrder(TgOrderInfo dto);

    /**
     * 冲正
     * @param orderId
     * @return
     */
    AjaxResult czOrder(Long orderId);

}
