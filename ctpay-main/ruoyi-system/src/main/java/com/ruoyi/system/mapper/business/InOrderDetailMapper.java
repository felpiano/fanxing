package com.ruoyi.system.mapper.business;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.system.domain.business.InOrderDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.dto.MerchantOrderReportDTO;
import com.ruoyi.system.domain.dto.MerchantQrcodeOrderDTO;
import com.ruoyi.system.domain.dto.OrderQueryDTO;
import com.ruoyi.system.domain.dto.ReportForAgentDTO;
import com.ruoyi.system.domain.vo.*;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 订单副表 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2024-10-31
 */
public interface InOrderDetailMapper extends BaseMapper<InOrderDetailEntity> {

    /**
     * 管理员、代理、商户查询订单
     * @param page
     * @param queryDTO
     * @return
     */
    Page<OrderVO> queryOrderList(Page<OrderVO> page, @Param("dto") OrderQueryDTO queryDTO);

    List<OrderVO> queryOrderListNoPage(@Param("dto") OrderQueryDTO queryDTO);

    /**
     * 管理员、代理、商户查询订单汇总
     * @param queryDTO
     * @return
     */
    OrderTotalVO queryOrderListTotal(@Param("dto") OrderQueryDTO queryDTO);
    /**
     * 订单统计
     * @param dto
     * @return
     */
    MerchantOrderReportVO reportOrderByDate(@Param("dto") MerchantOrderReportDTO dto);

    /**
     * 下级码商总金额统计
     * @param merchantIds
     * @param startTime
     * @param endTime
     * @return
     */
    BigDecimal allMerchantChildAmount(@Param("merchantIds") List<Long> merchantIds,@Param("startTime") String startTime,@Param("endTime") String endTime);

    /**
     * 根据码统计成功金额和成功单数
     * @param merchantId
     * @param startTime
     * @param endTime
     * @return
     */
    List<MerchantOrderReportVO> qrcodeOrders(@Param("merchantId")List<Long>  merchantId, @Param("startTime") String startTime,@Param("endTime") String endTime);

    /**
     * 根据码查询总成功金额
     * @param dto
     * @return
     */
    Page<MerchantQrcodeOrderVO> reportMerchantQrcodeOrders(Page page, @Param("dto") MerchantQrcodeOrderDTO dto);

    /**
     * 代理对账
     * @param dto
     * @return
     */
    List<ReportForAgentVO> reportForAgent(@Param("dto") ReportForAgentDTO dto);

    /**根据日期统计*/
    List<ReportForAgentVO> reportByDate(@Param("dto") ReportForAgentDTO dto);

    /**根据通道统计*/
    List<ReportForAgentVO> reportByChannel(@Param("dto") ReportForAgentDTO dto);

    /**根据通道和码商统计*/
    List<ReportForAgentVO> reportByChannelAndMerchant(@Param("dto") ReportForAgentDTO dto);

    /**根据商户统计*/
    List<ReportForAgentVO> reportByShop(@Param("dto") ReportForAgentDTO dto);

    /**码商获取子码商统计数据*/
    List<ReportForAgentVO> reportHomeForMerchant(@Param("dto") ReportForAgentDTO dto);

    /**码商获取子码商统计数据*/
    List<ReportForAgentVO> reportForMerchant(@Param("dto") ReportForAgentDTO dto);

    /**获取码商余额*/
    List<ReportForAgentVO> getMerchantBalance(@Param("agentId")Long agentId);

    List<CommissionVO> commissionList(@Param("dto") ReportForAgentDTO dto);

    List<ReportForAgentVO> allMerchantReport(@Param("dto") ReportForAgentDTO dto);

    BigDecimal sumBalanceOfMerchant(@Param("agentId") Long agentId);
}
