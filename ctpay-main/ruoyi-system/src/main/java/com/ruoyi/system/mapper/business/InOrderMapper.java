package com.ruoyi.system.mapper.business;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.business.InOrderEntity;
import com.ruoyi.system.domain.dto.OrderQueryDTO;
import com.ruoyi.system.domain.vo.MerchantOrderTotalVO;
import com.ruoyi.system.domain.vo.SuitableQrcodeVO;
import com.ruoyi.system.telegram.tgDataEntity.TgOrderInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2024-10-20
 */
public interface InOrderMapper extends BaseMapper<InOrderEntity> {

    /**
     * 定时任务，找出状态正常的单码，存入redis
     * @return
     */
    List<SuitableQrcodeVO> taskByCreateOrder();

    /**
     * 码商订单统计
     * @param dto
     * @return
     */
    MerchantOrderTotalVO merchantOrderTotal(@Param("dto") OrderQueryDTO dto);
    /**
     * 机器人转单，订单信息
     * @param dto
     * @return
     */
    TgOrderInfo queryTgOrder(@Param("dto")TgOrderInfo dto);

    InOrderEntity queryOrderByOnebuy(@Param("shopId") String shopId, @Param("amount") String amount, @Param("orderRemark") String orderRemark);
}
