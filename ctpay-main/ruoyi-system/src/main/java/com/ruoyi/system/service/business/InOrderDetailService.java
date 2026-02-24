package com.ruoyi.system.service.business;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.system.domain.business.InOrderDetailEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.dto.MerchantQrcodeOrderDTO;
import com.ruoyi.system.domain.dto.OrderQueryDTO;
import com.ruoyi.system.domain.dto.ReportForAgentDTO;
import com.ruoyi.system.domain.vo.*;

import java.util.List;

/**
 * <p>
 * 订单副表 服务类
 * </p>
 *
 * @author admin
 * @since 2024-10-31
 */
public interface InOrderDetailService extends IService<InOrderDetailEntity> {
    /**
     * 分页查询订单详情
     * @param page
     * @param queryDTO
     * @return
     */
    Page<OrderVO> queryOrderPage(Page<OrderVO> page, OrderQueryDTO queryDTO);

    List<OrderVO> queryOrderList(OrderQueryDTO queryDTO);

    OrderTotalVO queryOrderListTotal(OrderQueryDTO queryDTO);
    /**
     * 首页统计
     * @return
     */
    MerchantOrderReportVO reportOrderByDate();

    List<MerchantOrderReportVO> qrcodeOrders(List<Long> merchantId, String startTime, String endTime);

    /**
     * 代理首页统计
     * @param type 1-今日；2-昨日；3-本周
     * @return
     */
    MerchantOrderReportVO reportAgent(Integer type);

    Page<MerchantQrcodeOrderVO> reportMerchantQrcodeOrders(MerchantQrcodeOrderDTO dto);

    List<ReportForAgentVO> reportForAgent(ReportForAgentDTO dto);

    List<ReportForAgentVO> reportByCondition(ReportForAgentDTO dto);

    List<ReportForAgentVO> reportForMerchant(ReportForAgentDTO dto);

    List<ReportForAgentVO> reportForAgentByDate(ReportForAgentDTO dto);

    /**
     * 佣金列表
     * @param dto
     * @return
     */
    List<CommissionVO> commissionList(ReportForAgentDTO dto);

    List<ReportForAgentVO> allMerchantReport(ReportForAgentDTO dto);
}
