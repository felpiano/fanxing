package com.ruoyi.system.service.business.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.util.DateUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.enums.SysRoleEnum;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.business.InOrderDetailEntity;
import com.ruoyi.system.domain.business.MerchantAmountRecordsEntity;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.ruoyi.system.domain.business.MerchantHistoryBalanceEntity;
import com.ruoyi.system.domain.dto.MerchantOrderReportDTO;
import com.ruoyi.system.domain.dto.MerchantQrcodeOrderDTO;
import com.ruoyi.system.domain.dto.OrderQueryDTO;
import com.ruoyi.system.domain.dto.ReportForAgentDTO;
import com.ruoyi.system.domain.vo.*;
import com.ruoyi.system.mapper.business.InOrderDetailMapper;
import com.ruoyi.system.mapper.business.MerchantAmountRecordsMapper;
import com.ruoyi.system.mapper.business.MerchantHistoryBalanceMapper;
import com.ruoyi.system.service.business.InOrderDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.system.service.business.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单副表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-10-31
 */
@Service
public class InOrderDetailServiceImp extends ServiceImpl<InOrderDetailMapper, InOrderDetailEntity> implements InOrderDetailService {

    @Resource
    private MerchantService merchantService;
    @Resource
    private MerchantAmountRecordsMapper merchantAmountRecordsMapper;
    @Resource
    private MerchantHistoryBalanceMapper historyBalanceMapper;
    @Resource
    private RedisUtils redisUtils;
    @Autowired
    private MerchantHistoryBalanceMapper merchantHistoryBalanceMapper;

    @Override
    public Page<OrderVO> queryOrderPage(Page<OrderVO> page, OrderQueryDTO queryDTO) {
        return baseMapper.queryOrderList(page, queryDTO);
    }

    @Override
    public List<OrderVO> queryOrderList(OrderQueryDTO queryDTO) {
        return baseMapper.queryOrderListNoPage(queryDTO);
    }

    @Override
    public OrderTotalVO queryOrderListTotal(OrderQueryDTO queryDTO) {
        return baseMapper.queryOrderListTotal(queryDTO);
    }

    @Override
    public MerchantOrderReportVO reportOrderByDate() {
        MerchantOrderReportVO vo = new MerchantOrderReportVO();
        //当前码商
        MerchantEntity merchant = merchantService.getById(SecurityUtils.getUserId());
        //获取所有下级码商
        List<MerchantEntity> allList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantInfo), MerchantEntity.class);
        List<MerchantEntity> childList = merchantService.listAllChild(allList, merchant);


        /*余额*/
        //自身余额
        BigDecimal selfBalance = BigDecimal.ZERO;
        if (redisUtils.hasKey(RedisKeys.merchantBalance + merchant.getUserId())) {
            selfBalance = new BigDecimal(redisUtils.get(RedisKeys.merchantBalance + merchant.getUserId()));
        }
        BigDecimal childBalance = new BigDecimal(0);
        if (!childList.isEmpty()) {
            for (MerchantEntity data : childList) {
                if (redisUtils.hasKey(RedisKeys.merchantBalance + data.getUserId())) {
                    Object balance = redisUtils.get(RedisKeys.merchantBalance + data.getUserId());
                    if (ObjectUtil.isNotEmpty(balance)) {
                        childBalance = childBalance.add(new BigDecimal(balance.toString()));
                    }
                }
            }
        }
        vo.setChildMerchantBalance(childBalance);
        vo.setMerchantBalance(selfBalance.add(childBalance));
        //统计订单数据
        ReportForAgentDTO todayDto = new ReportForAgentDTO();
        todayDto.setChildPathStart(merchant.getParentPath());
        todayDto.setMerchantLevel(merchant.getMerchantLevel());
        /*今日统计*/
        //今日时间
        String startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(new Date()));
        String endTime = DateUtil.formatDateTime(DateUtil.endOfDay(new Date()));
        todayDto.setStartTime(startTime);
        todayDto.setEndTime(endTime);

        List<ReportForAgentVO> childTodayReport =  baseMapper.reportHomeForMerchant(todayDto);
        Integer totalCount = 0,childTotalCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO,childTotalAmount = BigDecimal.ZERO,merchantFee = BigDecimal.ZERO;
        if (childTodayReport != null && !childTodayReport.isEmpty()) {
            for (ReportForAgentVO rep : childTodayReport) {
                if (!rep.getUserId().equals(merchant.getUserId())) {
                   childTotalCount += rep.getSuccessCount();
                }
                totalCount += rep.getSuccessCount();
                if (!rep.getUserId().equals(merchant.getUserId())) {
                    childTotalAmount = childTotalAmount.add(rep.getSuccessMoney());
                }
                totalAmount = totalAmount.add(rep.getSuccessMoney());
                merchantFee = merchantFee.add(rep.getMerchantFee());
            }
        }
        vo.setTotalCount(totalCount);
        vo.setChildTotalCount(childTotalCount);
        vo.setTotalAmount(totalAmount);
        vo.setChildAmount(childTotalAmount);
        vo.setMerchantFee(merchantFee);

        /*昨日统计*/
        //昨日时间
        String ys = DateUtil.formatDateTime(DateUtil.beginOfDay(DateUtil.offsetDay(new Date(), -1)));
        String ye = DateUtil.formatDateTime(DateUtil.endOfDay(DateUtil.offsetDay(new Date(), -1)));
        todayDto.setStartTime(ys);
        todayDto.setEndTime(ye);
        List<ReportForAgentVO> childYesReport =  baseMapper.reportHomeForMerchant(todayDto);
        Integer totalCount1 = 0,childTotalCount1 = 0;
        BigDecimal totalAmount1 = BigDecimal.ZERO,childTotalAmount1 = BigDecimal.ZERO,yesMerchantFee = BigDecimal.ZERO;
        if (childYesReport != null && !childYesReport.isEmpty()) {
            for (ReportForAgentVO rep : childYesReport) {
                if (!rep.getUserId().equals(merchant.getUserId())) {
                    childTotalCount1 += rep.getSuccessCount();
                }
                totalCount1 += rep.getSuccessCount();
                if (!rep.getUserId().equals(merchant.getUserId())) {
                    childTotalAmount1 = childTotalAmount1.add(rep.getSuccessMoney());
                }
                totalAmount1 = totalAmount1.add(rep.getSuccessMoney());
                yesMerchantFee = yesMerchantFee.add(rep.getMerchantFee());
            }
        }
        vo.setYesTotalCount(totalCount1);
        vo.setYesChildTotalCount(childTotalCount1);
        vo.setYesTotalAmount(totalAmount1);
        vo.setYesChildAmount(childTotalAmount1);
        vo.setYesMerchantFee(yesMerchantFee);
        //昨日余额
        String yesDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1), "yyyy-MM-dd");
        vo.setYesBalance(balanceTotal(yesDate, childList, merchant));
        //前日余额
        String beforeYesDate = DateUtil.format(DateUtil.offsetDay(new Date(), -2), "yyyy-MM-dd");
        vo.setBeforeYesBalance(balanceTotal(beforeYesDate, childList, merchant));
        return vo;
    }

    private BigDecimal balanceTotal(String date,  List<MerchantEntity> childList, MerchantEntity merchant) {
        List<MerchantHistoryBalanceEntity> beforeBalanceList = merchantHistoryBalanceMapper.selectList(Wrappers.lambdaQuery(MerchantHistoryBalanceEntity.class)
                .eq(MerchantHistoryBalanceEntity::getDataDt, date));
        if (beforeBalanceList != null && !beforeBalanceList.isEmpty()) {
            beforeBalanceList.forEach(data -> {
                data.setBalance(data.getBalance().add(data.getFreezeAmount()));
            });
            Map<Long,BigDecimal> beforeBalanceMap = beforeBalanceList.stream().collect(Collectors.toMap(MerchantHistoryBalanceEntity::getUserId, MerchantHistoryBalanceEntity::getBalance));
            BigDecimal balanceTotal = BigDecimal.ZERO;
            if (!ObjectUtil.isEmpty(beforeBalanceMap.get(merchant.getUserId()))) {
                balanceTotal = balanceTotal.add(beforeBalanceMap.get(merchant.getUserId()));
            }
            if (!childList.isEmpty()) {
                for (MerchantEntity data : childList) {
                    if (!ObjectUtil.isEmpty(beforeBalanceMap.get(data.getUserId()))){
                        balanceTotal = balanceTotal.add(data.getBalance());
                    }
                }
            }
            return balanceTotal;
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<MerchantOrderReportVO> qrcodeOrders(List<Long>  merchantId, String startTime, String endTime) {
        return baseMapper.qrcodeOrders(merchantId, startTime, endTime);
    }

    @Override
    public MerchantOrderReportVO reportAgent(Integer type) {
        Long agentId = SecurityUtils.getUserId();
        String startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(new Date()));
        String endTime = DateUtil.formatDateTime(DateUtil.endOfDay(new Date()));
        if (type == 2) {
            startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateUtil.offsetDay(new Date(), -1)));
            endTime = DateUtil.formatDateTime(DateUtil.endOfDay(DateUtil.offsetDay(new Date(), -1)));
        } else if (type == 3) {
            startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(new Date()));
            endTime = DateUtil.formatDateTime(DateUtil.endOfWeek(new Date()));
        }
        MerchantOrderReportDTO dto = new MerchantOrderReportDTO();
        dto.setAgentId(agentId);
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        MerchantOrderReportVO vo = baseMapper.reportOrderByDate(dto);
        if (vo != null) {
            BigDecimal sumBalance = baseMapper.sumBalanceOfMerchant(agentId);
            vo.setMerchantBalance(sumBalance);
        }
        return vo;
    }

    /**
     * 代理根据通道查询可用码商等数据
     */
    @Override
    public Page<MerchantQrcodeOrderVO> reportMerchantQrcodeOrders(MerchantQrcodeOrderDTO dto) {
        Page<MerchantQrcodeOrderVO> rowPage = new Page<>(dto.getPageNum(), dto.getPageSize());
        if (StrUtil.isEmpty(dto.getStartDate())) {
            String startDate = DateUtils.format(new Date(), "yyyy-MM-dd");
            dto.setStartDate(startDate);
        }
        if (StrUtil.isEmpty(dto.getEndDate())) {
            String endDate = DateUtils.format(new Date(), "yyyy-MM-dd");
            dto.setEndDate(endDate);
        }
        dto.setStartDate(dto.getStartDate() + " 00:00:00");
        dto.setEndDate(dto.getEndDate() + " 23:59:59");
        Page<MerchantQrcodeOrderVO> page = baseMapper.reportMerchantQrcodeOrders(rowPage, dto);
        if (page != null && page.getRecords() != null) {
            page.getRecords().forEach(data -> {
                if (data.getTotalCount() != null && data.getTotalCount() > 0 && data.getSuccessCount() != null) {
                    data.setSuccessRate(new BigDecimal(data.getSuccessCount()).divide(new BigDecimal(data.getTotalCount()), 2, RoundingMode.HALF_UP));
                }
            });
        }
        return page;
    }

    @Override
    public List<ReportForAgentVO> reportForAgent(ReportForAgentDTO dto) {
        List<ReportForAgentVO> list = baseMapper.reportForAgent(dto);
        if (list != null && !list.isEmpty()) {
            List<ReportForAgentVO> balanceList = baseMapper.getMerchantBalance(dto.getAgentId());
            Map<Long, ReportForAgentVO> map = new HashMap<>();
            if (balanceList != null && !balanceList.isEmpty()) {
                map = balanceList.stream().collect(Collectors.toMap(ReportForAgentVO::getUserId, a -> a));
            }
            ReportForAgentVO vo = new ReportForAgentVO();
            vo.setUserName("合计");
            vo.setShopName("合计");
            Integer totalCount = 0,successCount = 0;
            BigDecimal totalMoney = BigDecimal.ZERO,successMoney= BigDecimal.ZERO,chargeMoney = BigDecimal.ZERO,
                    yesBalance= BigDecimal.ZERO,todayBalance = BigDecimal.ZERO,merchantFee = BigDecimal.ZERO;

            for (ReportForAgentVO data : list) {
                if (map.get(data.getUserId()) != null) {
                    data.setTodayBalance(map.get(data.getUserId()).getTodayBalance());
                    data.setYesBalance(map.get(data.getUserId()).getYesBalance());
                    //余额
                    if (redisUtils.hasKey(RedisKeys.merchantBalance + data.getUserId())) {
                        Object balance = redisUtils.get(RedisKeys.merchantBalance + data.getUserId());
                        if (ObjectUtil.isNotEmpty(balance)) {
                            data.setTodayBalance(new BigDecimal(balance.toString()));
                        }
                    }
                }

                if (ObjectUtil.isNotNull(data.getTotalCount())) {
                    totalCount +=  data.getTotalCount();
                }
                if (ObjectUtil.isNotNull(data.getSuccessCount())) {
                    successCount +=  data.getSuccessCount();
                }
                if (ObjectUtil.isNotNull(data.getTotalMoney())) {
                    totalMoney = totalMoney.add(data.getTotalMoney());
                }
                if (ObjectUtil.isNotNull(data.getSuccessMoney())) {
                    successMoney = successMoney.add(data.getSuccessMoney());
                }
                if (ObjectUtil.isNotNull(data.getYesBalance())) {
                    yesBalance = yesBalance.add(data.getYesBalance());
                }
                if (ObjectUtil.isNotNull(data.getTodayBalance())) {
                    todayBalance = todayBalance.add(data.getTodayBalance());
                }
                if (ObjectUtil.isNotNull(data.getMerchantFee())) {
                    merchantFee = merchantFee.add(data.getMerchantFee());
                }
                if (ObjectUtil.isNotNull(data.getChargeMoney())) {
                    chargeMoney = chargeMoney.add(data.getChargeMoney());
                }
                if (data.getTotalCount() != null && data.getSuccessCount() != null && data.getTotalCount() != 0) {
                    data.setSuccessRate(new BigDecimal(data.getSuccessCount()).divide(new BigDecimal(data.getTotalCount()),4, RoundingMode.HALF_UP));
                }
            }
            vo.setTotalCount(totalCount);
            vo.setSuccessCount(successCount);
            vo.setTotalMoney(totalMoney);
            vo.setSuccessMoney(successMoney);
            vo.setYesBalance(yesBalance);
            vo.setMerchantFee(merchantFee);
            vo.setTodayBalance(todayBalance);
            vo.setChargeMoney(chargeMoney);
            if (vo.getTotalCount() != null && vo.getSuccessCount() != null && vo.getTotalCount() != 0) {
                vo.setSuccessRate(new BigDecimal(vo.getSuccessCount()).divide(new BigDecimal(vo.getTotalCount()),4, RoundingMode.HALF_UP));
            }
            list.add(vo);
        }
        return list;
    }

    @Override
    public List<ReportForAgentVO> reportByCondition(ReportForAgentDTO dto) {
        if (StrUtil.isEmpty(dto.getStartTime())) {
            dto.setStartTime(DateUtil.formatDateTime(DateUtil.beginOfDay(new Date())));
        }
        if (StrUtil.isEmpty(dto.getEndTime())) {
            dto.setEndTime(DateUtil.formatDateTime(DateUtil.endOfDay(new Date())));
        }
        List<ReportForAgentVO> list;
         if (dto.getType() == 1) {
            list = baseMapper.reportByShop(dto);
        }else if (dto.getType() == 2) {
            list = baseMapper.reportByDate(dto);
        }else if (dto.getType() == 3) {
             if (ObjectUtil.isNotEmpty(dto.getMerchantId())) {
                 MerchantEntity merchant = merchantService.getById(dto.getMerchantId());
                 dto.setChildPathStart(merchant.getParentPath());
                 dto.setMerchantId(null);
             }
            list = baseMapper.reportByChannelAndMerchant(dto);
        }else {
             list = new ArrayList<>();
         }
        if (list != null && !list.isEmpty()) {
            ReportForAgentVO vo = new ReportForAgentVO();
            vo.setUserName("合计");
            vo.setShopName("合计");
            Integer totalCount = 0,successCount = 0;
            BigDecimal totalMoney = BigDecimal.ZERO,successMoney= BigDecimal.ZERO;
            for (ReportForAgentVO data : list) {
                if (ObjectUtil.isNotNull(data.getTotalCount())) {
                    totalCount +=  data.getTotalCount();
                }
                if (ObjectUtil.isNotNull(data.getSuccessCount())) {
                    successCount +=  data.getSuccessCount();
                }
                if (ObjectUtil.isNotNull(data.getTotalMoney())) {
                    totalMoney = totalMoney.add(data.getTotalMoney());
                }
                if (ObjectUtil.isNotNull(data.getSuccessMoney())) {
                    successMoney = successMoney.add(data.getSuccessMoney());
                }
                if (data.getTotalCount() != null && data.getSuccessCount() != null && data.getTotalCount() != 0) {
                    data.setSuccessRate(new BigDecimal(data.getSuccessCount()).divide(new BigDecimal(data.getTotalCount()),4, RoundingMode.HALF_UP));
                }
            }
            vo.setTotalCount(totalCount);
            vo.setSuccessCount(successCount);
            vo.setTotalMoney(totalMoney);
            vo.setSuccessMoney(successMoney);
            if (vo.getTotalCount() != null && vo.getSuccessCount() != null && vo.getTotalCount() != 0) {
                vo.setSuccessRate(new BigDecimal(vo.getSuccessCount()).divide(new BigDecimal(vo.getTotalCount()),4, RoundingMode.HALF_UP));
            }
            list.add(vo);
        }
        return list;
    }

    @Override
    public List<ReportForAgentVO> reportForMerchant(ReportForAgentDTO dto) {
        List<ReportForAgentVO> resultList = new ArrayList<>();
        //当前码商
        MerchantEntity curr = merchantService.getById(SecurityUtils.getUserId());
        dto.setChildPathStart(curr.getParentPath());
        //下级码商
        List<MerchantEntity> childList = merchantService.list(Wrappers.lambdaQuery(MerchantEntity.class)
                .likeRight(MerchantEntity::getParentPath, curr.getParentPath())
                .eq(StrUtil.isNotEmpty(dto.getMerchantName()), MerchantEntity::getUserName, dto.getMerchantName())
                .eq(ObjectUtil.isNotEmpty(dto.getMerchantId()), MerchantEntity::getUserId, dto.getMerchantId())
        );
        if (childList != null && !childList.isEmpty()) {
            List<MerchantEntity> allMerchantList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantInfo), MerchantEntity.class);
            //获取所有下级的统计数据
            List<ReportForAgentVO> reportList = baseMapper.reportForMerchant(dto);
            Map<Long, ReportForAgentVO> reportMap = new HashMap<>();
            if (reportList != null && !reportList.isEmpty()) {
                reportMap = reportList.stream().collect(Collectors.toMap(ReportForAgentVO::getUserId, a -> a, (key1,key2) -> key2));
            }
            //获取时间段内的所有码商佣金汇总
            List<MerchantEntity> freezeList = merchantAmountRecordsMapper.getFreezeByUserDate(dto.getStartTime(), dto.getEndTime());
            Map<Long, BigDecimal> freezeMap = new HashMap<>();
            if (freezeList != null && !freezeList.isEmpty()) {
                freezeMap = freezeList.stream().collect(Collectors.toMap(MerchantEntity::getUserId, MerchantEntity::getFreezeAmount));
            }
            //获取时间段内的所有码商上分汇总
            List<MerchantAmountRecordsEntity> jifenList = merchantAmountRecordsMapper.getMoneyGroupByUserId(dto.getStartTime(), dto.getEndTime());
            Map<Long, BigDecimal> jifenMap = new HashMap<>();
            if (jifenList != null && !jifenList.isEmpty()) {
                jifenMap = jifenList.stream().collect(Collectors.toMap(MerchantAmountRecordsEntity::getUserId, MerchantAmountRecordsEntity::getChangeAmount));
            }

            String startDate = DateUtil.format(DateUtil.offsetDay(DateUtil.parse(dto.getStartTime(), "yyyy-MM-dd HH:mm:ss"), -1), "yyyy-MM-dd");
            String endDate = DateUtil.format(DateUtil.parse(dto.getEndTime(), "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd");
            ReportForAgentVO vo = new ReportForAgentVO();
            vo.setUserName("合计");
            vo.setShopName("合计");
            Integer totalCount = 0, successCount = 0;
            BigDecimal totalMoney = BigDecimal.ZERO, successMoney = BigDecimal.ZERO, chargeMoney = BigDecimal.ZERO,
                    yesBalance = BigDecimal.ZERO, todayBalance = BigDecimal.ZERO, merchantFee = BigDecimal.ZERO;
            Map<Long,BigDecimal> beforeBalanceMap = new HashMap<>();
            List<MerchantHistoryBalanceEntity> beforeBalanceList = merchantHistoryBalanceMapper.selectList(Wrappers.lambdaQuery(MerchantHistoryBalanceEntity.class)
                    .eq(MerchantHistoryBalanceEntity::getDataDt, startDate));
            if (beforeBalanceList != null && !beforeBalanceList.isEmpty()) {
                beforeBalanceList.forEach(data -> {
                    data.setBalance(data.getBalance().add(data.getFreezeAmount()));
                });
                beforeBalanceMap = beforeBalanceList.stream().collect(Collectors.toMap(MerchantHistoryBalanceEntity::getUserId, MerchantHistoryBalanceEntity::getBalance));
            }
            Map<Long,BigDecimal> afterBalanceMap = new HashMap<>();
            if (!StrUtil.equals(endDate, DateUtil.format(new Date(), "yyyy-MM-dd"))) {
                List<MerchantHistoryBalanceEntity> afterBalanceList = merchantHistoryBalanceMapper.selectList(Wrappers.lambdaQuery(MerchantHistoryBalanceEntity.class)
                        .eq(MerchantHistoryBalanceEntity::getDataDt, endDate));
                if (afterBalanceList != null && !afterBalanceList.isEmpty()) {
                    afterBalanceList.forEach(data -> {
                        data.setBalance(data.getBalance().add(data.getFreezeAmount()));
                    });
                    afterBalanceMap = afterBalanceList.stream().collect(Collectors.toMap(MerchantHistoryBalanceEntity::getUserId, MerchantHistoryBalanceEntity::getBalance));
                }
            }
            List<MerchantEntity> currChildList = childList.stream().filter(a -> a.getParentId().equals(curr.getUserId())).collect(Collectors.toList());
            for (MerchantEntity firstChild : currChildList) {
                ReportForAgentVO rvo = BeanUtil.copyProperties(firstChild, ReportForAgentVO.class);
                //获取包含fistChild在内的所有码商
                List<MerchantEntity> allChildList = allMerchantList.stream().filter(
                        a -> a.getParentPath().startsWith(firstChild.getParentPath())
                ).collect(Collectors.toList());
                if (!allChildList.isEmpty()) {
                    rvo.setTotalMoney(BigDecimal.ZERO);
                    rvo.setSuccessMoney(BigDecimal.ZERO);
                    rvo.setTotalCount(0);
                    rvo.setSuccessCount(0);
                    rvo.setTodayBalance(BigDecimal.ZERO);
                    rvo.setTodayFreeze(BigDecimal.ZERO);
                    rvo.setYesBalance(BigDecimal.ZERO);
                    rvo.setYesFreeze(BigDecimal.ZERO);
                    rvo.setMerchantFee(BigDecimal.ZERO);
                    rvo.setChargeMoney(BigDecimal.ZERO);
                    rvo.setMerchantLevel(firstChild.getMerchantLevel());
                    for (MerchantEntity merchant : allChildList) {
                        ReportForAgentVO mrvo = reportMap.get(merchant.getUserId());
                        if (mrvo != null) {
                            rvo.setTotalCount(rvo.getTotalCount() + mrvo.getTotalCount());
                            rvo.setSuccessCount(rvo.getSuccessCount() + mrvo.getSuccessCount());
                            rvo.setTotalMoney(rvo.getTotalMoney().add(mrvo.getTotalMoney()));
                            rvo.setSuccessMoney(rvo.getSuccessMoney().add(mrvo.getSuccessMoney()));
                        }
                        //时间段内佣金
                        if (freezeMap.get(merchant.getUserId()) != null) {
                            rvo.setMerchantFee(rvo.getMerchantFee().add(freezeMap.get(merchant.getUserId())));
                        }
                        //开始余额
                        if (beforeBalanceMap.get(merchant.getUserId()) != null) {
                            rvo.setYesBalance(rvo.getYesBalance().add(beforeBalanceMap.get(merchant.getUserId())));
                        }
                        //上分
                        if (jifenMap.get(merchant.getUserId()) != null) {
                            rvo.setChargeMoney(rvo.getChargeMoney().add(jifenMap.get(merchant.getUserId())));
                        }
                        //如果结束日期是今天，则从redis中拿最新数据。否则从历史余额表中获取
                        if (StrUtil.equals(endDate, DateUtil.format(new Date(), "yyyy-MM-dd"))) {
                            //余额
                            if (redisUtils.hasKey(RedisKeys.merchantBalance + merchant.getUserId())) {
                                Object balance = redisUtils.get(RedisKeys.merchantBalance + merchant.getUserId());
                                if (ObjectUtil.isNotEmpty(balance)) {
                                    rvo.setTodayBalance(rvo.getTodayBalance().add(new BigDecimal(balance.toString())));
                                }
                            }
                            //佣金
                            if (redisUtils.hasKey(RedisKeys.merchantCommission + merchant.getUserId())) {
                                Object commission = redisUtils.get(RedisKeys.merchantCommission + merchant.getUserId());
                                if (ObjectUtil.isNotEmpty(commission)) {
                                    rvo.setTodayBalance(rvo.getTodayBalance().add(new BigDecimal(commission.toString())));
                                }
                            }
                        } else {
                            if (afterBalanceMap.get(merchant.getUserId()) != null) {
                                rvo.setTodayBalance(rvo.getTodayBalance().add(afterBalanceMap.get(merchant.getUserId())));
                            }
                        }
                    }
                }
                //是否有跑量
                if (dto.getOrderFlag() != null && dto.getOrderFlag() == 1 && (rvo.getSuccessMoney().compareTo(BigDecimal.ZERO) > 0
                    || (rvo.getChargeMoney() != null && rvo.getChargeMoney().compareTo(BigDecimal.ZERO) != 0)
                )) {
                    resultList.add(rvo);
                } else if (ObjectUtil.isNull(dto.getOrderFlag()) || dto.getOrderFlag() != 1) {
                    resultList.add(rvo);
                }

                //汇总
                if (ObjectUtil.isNotNull(rvo.getTotalCount())) {
                    totalCount += rvo.getTotalCount();
                }
                if (ObjectUtil.isNotNull(rvo.getSuccessCount())) {
                    successCount += rvo.getSuccessCount();
                }
                if (ObjectUtil.isNotNull(rvo.getTotalMoney())) {
                    totalMoney = totalMoney.add(rvo.getTotalMoney());
                }
                if (ObjectUtil.isNotNull(rvo.getSuccessMoney())) {
                    successMoney = successMoney.add(rvo.getSuccessMoney());
                }
                if (ObjectUtil.isNotNull(rvo.getYesBalance())) {
                    yesBalance = yesBalance.add(rvo.getYesBalance());
                }
                if (ObjectUtil.isNotNull(rvo.getTodayBalance())) {
                    todayBalance = todayBalance.add(rvo.getTodayBalance());
                }
                if (ObjectUtil.isNotNull(rvo.getMerchantFee())) {
                    merchantFee = merchantFee.add(rvo.getMerchantFee());
                }
                if (ObjectUtil.isNotNull(rvo.getChargeMoney())) {
                    chargeMoney = chargeMoney.add(rvo.getChargeMoney());
                }
                if (rvo.getTotalCount() != null && rvo.getSuccessCount() != null && rvo.getTotalCount() != 0) {
                    rvo.setSuccessRate(new BigDecimal(rvo.getSuccessCount()).divide(new BigDecimal(rvo.getTotalCount()), 4, RoundingMode.HALF_UP));
                }
            }
            vo.setTotalCount(totalCount);
            vo.setSuccessCount(successCount);
            vo.setTotalMoney(totalMoney);
            vo.setSuccessMoney(successMoney);
            vo.setYesBalance(yesBalance);
            vo.setMerchantFee(merchantFee);
            vo.setTodayBalance(todayBalance);
            vo.setChargeMoney(chargeMoney);
            if (vo.getTotalCount() != null && vo.getSuccessCount() != null && vo.getTotalCount() != 0) {
                vo.setSuccessRate(new BigDecimal(vo.getSuccessCount()).divide(new BigDecimal(vo.getTotalCount()), 4, RoundingMode.HALF_UP));
            }
            resultList.add(vo);
        }
        return resultList;
    }

    @Override
    public List<ReportForAgentVO> reportForAgentByDate(ReportForAgentDTO dto) {
        if (StrUtil.isEmpty(dto.getStartTime())) {
            dto.setStartTime(DateUtil.formatDateTime(DateUtil.beginOfDay(new Date())));
        } else {
            dto.setStartTime(DateUtil.formatDateTime(DateUtil.beginOfDay(DateUtil.parse(dto.getStartTime(), "yyyy-MM-dd"))));
        }
        if (StrUtil.isEmpty(dto.getEndTime())) {
            dto.setEndTime(DateUtil.formatDateTime(DateUtil.endOfDay(new Date())));
        } else {
            dto.setEndTime(DateUtil.formatDateTime(DateUtil.endOfDay(DateUtil.parse(dto.getEndTime(), "yyyy-MM-dd"))));
        }
        System.out.println("1");
        List<ReportForAgentVO> list = baseMapper.reportForAgent(dto);
        if (list != null && !list.isEmpty()) {
            ReportForAgentVO vo = new ReportForAgentVO();
            vo.setUserName("合计");
            vo.setShopName("合计");
            Integer totalCount = 0,successCount = 0;
            BigDecimal totalMoney = BigDecimal.ZERO,successMoney= BigDecimal.ZERO,chargeMoney = BigDecimal.ZERO,
                    yesBalance= BigDecimal.ZERO,todayBalance = BigDecimal.ZERO,merchantFee = BigDecimal.ZERO;
            String startDate = DateUtil.format(DateUtil.offsetDay(DateUtil.parse(dto.getStartTime(), "yyyy-MM-dd HH:mm:ss"),-1), "yyyy-MM-dd");
            String endDate = DateUtil.format(DateUtil.parse(dto.getEndTime(), "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd");
            List<MerchantEntity> allMerchantList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantInfo), MerchantEntity.class);
            System.out.println("2");
            List<MerchantEntity> freezeList = merchantAmountRecordsMapper.getFreezeByDate(dto.getStartTime(), dto.getEndTime());
            Map<Long,BigDecimal> freezeMap = new HashMap<>();
            if (freezeList != null && !freezeList.isEmpty()) {
                freezeMap = freezeList.stream().collect(Collectors.toMap(MerchantEntity::getUserId, MerchantEntity::getFreezeAmount));
            }
            //开始结束余额
            Map<Long,BigDecimal> startBalanceMap = new HashMap<>();
            Map<Long,BigDecimal> endBalanceMap = new HashMap<>();
            Map<Long,BigDecimal> endCommissionMap = new HashMap<>();
            System.out.println("3");
            List<MerchantHistoryBalanceEntity> startBalanceList = merchantHistoryBalanceMapper.selectList(Wrappers.lambdaQuery(MerchantHistoryBalanceEntity.class)
                    .eq(MerchantHistoryBalanceEntity::getDataDt, startDate));
            if (startBalanceList != null && !startBalanceList.isEmpty()) {
                startBalanceList.forEach(data -> {
                    data.setBalance(data.getBalance().add(data.getFreezeAmount()));
                });
                startBalanceMap = startBalanceList.stream().collect(Collectors.toMap(MerchantHistoryBalanceEntity::getUserId, MerchantHistoryBalanceEntity::getBalance));
            }
            System.out.println("4");
            //结束日期是今天，则从redis获取余额
            if (StrUtil.equals(endDate, DateUtil.format(new Date(), "yyyy-MM-dd"))) {
                List<Object> balanceKeys = new ArrayList<>();
                List<Object> commissionKeys = new ArrayList<>();
                allMerchantList.forEach(data -> {
                    balanceKeys.add(RedisKeys.merchantBalance + data.getUserId());
                    commissionKeys.add(RedisKeys.merchantCommission + data.getUserId());
                });
                Map<Object, Object> balanceObject = redisUtils.batchQueryKeysAndValues(balanceKeys);
                Map<Object, Object> commissionObject = redisUtils.batchQueryKeysAndValues(commissionKeys);
                for (MerchantEntity data : allMerchantList) {
                    if (ObjectUtil.isNotNull(balanceObject.get(RedisKeys.merchantBalance + data.getUserId()))) {
                        endBalanceMap.put(data.getUserId(), new BigDecimal(balanceObject.get(RedisKeys.merchantBalance + data.getUserId()).toString()));
                    } else {
                        endBalanceMap.put(data.getUserId(), new BigDecimal(0));
                    }
                    if (ObjectUtil.isNotNull(commissionObject.get(RedisKeys.merchantCommission + data.getUserId()))) {
                        endCommissionMap.put(data.getUserId(), new BigDecimal(commissionObject.get(RedisKeys.merchantCommission + data.getUserId()).toString()));
                    } else {
                        endCommissionMap.put(data.getUserId(), new BigDecimal(0));
                    }
                }
            } else {
                List<MerchantHistoryBalanceEntity> endBalanceList = merchantHistoryBalanceMapper.selectList(Wrappers.lambdaQuery(MerchantHistoryBalanceEntity.class)
                        .eq(MerchantHistoryBalanceEntity::getDataDt, endDate));
                if (endBalanceList != null && !endBalanceList.isEmpty()) {
                    endBalanceList.forEach(data -> {
                        data.setBalance(data.getBalance().add(data.getFreezeAmount()));
                    });
                    endBalanceMap = endBalanceList.stream().collect(Collectors.toMap(MerchantHistoryBalanceEntity::getUserId, MerchantHistoryBalanceEntity::getBalance));
                }
            }
            System.out.println("5");
            for (ReportForAgentVO data : list) {
                MerchantEntity merchant = BeanUtil.copyProperties(data, MerchantEntity.class);
                //所有下级码商
                List<MerchantEntity> child = merchantService.listAllChild(allMerchantList, merchant);
                //如果结束日期是今天，则从redis中拿最新数据。否则从历史余额表中获取
                if (StrUtil.equals(endDate, DateUtil.format(new Date(), "yyyy-MM-dd"))) {
                    System.out.println("开始获取redis余额");
                    BigDecimal todayBalanceRedis = BigDecimal.ZERO;
                    BigDecimal todayFreezeRedis = BigDecimal.ZERO;
                    //自己的余额
                    BigDecimal selfBalance = endBalanceMap.get(data.getUserId());
                    if (ObjectUtil.isNotEmpty(selfBalance)) {
                        todayBalanceRedis = todayBalanceRedis.add(selfBalance);
                    }
                    /*if (redisUtils.hasKey(RedisKeys.merchantBalance + merchant.getUserId())) {
                        Object balance = redisUtils.get(RedisKeys.merchantBalance + merchant.getUserId());
                        if (ObjectUtil.isNotEmpty(balance)) {
                            todayBalanceRedis = todayBalanceRedis.add(new BigDecimal(balance.toString()));
                        }
                    }*/
                    //自己的佣金
                    BigDecimal selfCommission = endCommissionMap.get(data.getUserId());
                    if (ObjectUtil.isNotEmpty(selfCommission)) {
                        todayFreezeRedis = todayFreezeRedis.add(new BigDecimal(selfCommission.toString()));
                    }
//                    if (redisUtils.hasKey(RedisKeys.merchantCommission + merchant.getUserId())) {
//                        Object commission = redisUtils.get(RedisKeys.merchantCommission + merchant.getUserId());
//                        if (ObjectUtil.isNotEmpty(commission)) {
//                            todayFreezeRedis = todayFreezeRedis.add(new BigDecimal(commission.toString()));
//                        }
//                    }
                    //下级的余额和佣金
                    if (child != null && !child.isEmpty()) {
                        for (MerchantEntity c : child) {
                            //余额
                            BigDecimal balance = endBalanceMap.get(c.getUserId());
                            if (ObjectUtil.isNotNull(balance)) {
                                todayBalanceRedis = todayBalanceRedis.add(balance);
                            }
//                            if (redisUtils.hasKey(RedisKeys.merchantBalance + childMerchent.getUserId())) {
//                                Object balance = redisUtils.get(RedisKeys.merchantBalance + childMerchent.getUserId());
//                                if (ObjectUtil.isNotEmpty(balance)) {
//                                    todayBalanceRedis = todayBalanceRedis.add(new BigDecimal(balance.toString()));
//                                }
//                            }
                            //佣金
//                            if (redisUtils.hasKey(RedisKeys.merchantCommission + childMerchent.getUserId())) {
//                                Object commission = redisUtils.get(RedisKeys.merchantCommission + childMerchent.getUserId());
//                                if (ObjectUtil.isNotEmpty(commission)) {
//                                    todayFreezeRedis = todayFreezeRedis.add(new BigDecimal(commission.toString()));
//                                }
//                            }
                            BigDecimal commission = endCommissionMap.get(c.getUserId());
                            if (ObjectUtil.isNotNull(commission)) {
                                todayFreezeRedis = todayFreezeRedis.add(commission);
                            }
                        }
                    }
                    System.out.println("结束获取redis余额");
                    data.setTodayBalance(todayBalanceRedis);
                    data.setTodayFreeze(todayFreezeRedis);
                } else {
                    BigDecimal selfBalance = endBalanceMap.get(data.getUserId());
                    if (ObjectUtil.isEmpty(selfBalance)){
                        selfBalance = BigDecimal.ZERO;
                    }
                    if (child != null && !child.isEmpty()) {
                        for (MerchantEntity c : child) {
                            if (ObjectUtil.isNotEmpty(endBalanceMap.get(c.getUserId()))) {
                                selfBalance = selfBalance.add(endBalanceMap.get(c.getUserId()));
                            }
                        }
                    }
                    data.setTodayBalance(selfBalance);
                    data.setTodayFreeze(BigDecimal.ZERO);
//                    MerchantHistoryBalanceEntity aa = historyBalanceMapper.totalBalanceByParentPath(endDate, data.getParentPath());
//                    if (ObjectUtil.isNotEmpty(aa)) {
//                        data.setTodayBalance(ObjectUtil.isNotEmpty(aa.getBalance()) ? aa.getBalance() : BigDecimal.ZERO);
//                        data.setTodayFreeze(BigDecimal.ZERO);
//                    } else {
//                        data.setTodayBalance(BigDecimal.ZERO);
//                        data.setTodayFreeze(BigDecimal.ZERO);
//                    }
                }
//                MerchantHistoryBalanceEntity aa = historyBalanceMapper.totalBalanceByParentPath(startDate, data.getParentPath());
//                data.setYesBalance(ObjectUtil.isNotNull(aa) && ObjectUtil.isNotEmpty(aa.getBalance())?aa.getBalance():BigDecimal.ZERO);
                BigDecimal startSelf = startBalanceMap.get(data.getUserId());
                if (ObjectUtil.isEmpty(startSelf)){
                    startSelf = BigDecimal.ZERO;
                }
                if (child != null && !child.isEmpty()) {
                    for (MerchantEntity c : child) {
                        if (ObjectUtil.isNotEmpty(startBalanceMap.get(c.getUserId()))) {
                            startSelf = startSelf.add(startBalanceMap.get(c.getUserId()));
                        }
                    }
                }
                data.setYesBalance(startSelf);
                data.setYesFreeze(BigDecimal.ZERO);
                /*今日/昨日余额=今日/昨日余额+今日/昨日佣金*/
                data.setYesBalance(data.getYesBalance().add(data.getYesFreeze()));
                data.setTodayBalance(data.getTodayBalance().add(data.getTodayFreeze()));

                //统计日期内的佣金
                data.setMerchantFee(freezeMap.get(data.getUserId()));
                //汇总
                if (ObjectUtil.isNotNull(data.getTotalCount())) {
                    totalCount +=  data.getTotalCount();
                }
                if (ObjectUtil.isNotNull(data.getSuccessCount())) {
                    successCount +=  data.getSuccessCount();
                }
                if (ObjectUtil.isNotNull(data.getTotalMoney())) {
                    totalMoney = totalMoney.add(data.getTotalMoney());
                }
                if (ObjectUtil.isNotNull(data.getSuccessMoney())) {
                    successMoney = successMoney.add(data.getSuccessMoney());
                }
                if (ObjectUtil.isNotNull(data.getYesBalance())) {
                    yesBalance = yesBalance.add(data.getYesBalance());
                }
                if (ObjectUtil.isNotNull(data.getTodayBalance())) {
                    todayBalance = todayBalance.add(data.getTodayBalance());
                }
                if (ObjectUtil.isNotNull(data.getMerchantFee())) {
                    merchantFee = merchantFee.add(data.getMerchantFee());
                }
                if (ObjectUtil.isNotNull(data.getChargeMoney())) {
                    chargeMoney = chargeMoney.add(data.getChargeMoney());
                }
                if (data.getTotalCount() != null && data.getSuccessCount() != null && data.getTotalCount() != 0) {
                    data.setSuccessRate(new BigDecimal(data.getSuccessCount()).divide(new BigDecimal(data.getTotalCount()),4, RoundingMode.HALF_UP));
                }
            }
            System.out.println("6");
            vo.setTotalCount(totalCount);
            vo.setSuccessCount(successCount);
            vo.setTotalMoney(totalMoney);
            vo.setSuccessMoney(successMoney);
            vo.setYesBalance(yesBalance);
            vo.setMerchantFee(merchantFee);
            vo.setTodayBalance(todayBalance);
            vo.setChargeMoney(chargeMoney);
            if (vo.getTotalCount() != null && vo.getSuccessCount() != null && vo.getTotalCount() != 0) {
                vo.setSuccessRate(new BigDecimal(vo.getSuccessCount()).divide(new BigDecimal(vo.getTotalCount()),4, RoundingMode.HALF_UP));
            }
            list.add(vo);
        }
        return list;
    }

    @Override
    public List<CommissionVO> commissionList(ReportForAgentDTO dto) {
        List<CommissionVO> list = baseMapper.commissionList(dto);
        //获取佣金
        CommissionVO vo = CommissionVO.builder()
                .merchantName("合计")
                .channelName("合计")
                .totalAmount(BigDecimal.ZERO)
                .totalCount(0)
                .commissionFour(BigDecimal.ZERO)
                .commissionThree(BigDecimal.ZERO)
                .commissionTwo(BigDecimal.ZERO)
                .commissionOne(BigDecimal.ZERO)
                .myCommission(BigDecimal.ZERO)
                .build();
        if (list != null && !list.isEmpty()) {
            List<MerchantEntity> merchantList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantInfo), MerchantEntity.class);
            Map<String, MerchantEntity> merchantMap = merchantList.stream().collect(Collectors.toMap(a -> a.getUserId().toString(), a -> a, (key1, key2) -> key2));
            list.forEach(data -> {
                vo.setMyCommission(data.getMyCommission().add(vo.getMyCommission()));
                vo.setCommissionFour(data.getCommissionFour().add(vo.getCommissionFour()));
                vo.setCommissionThree(data.getCommissionThree().add(vo.getCommissionThree()));
                vo.setCommissionTwo(data.getCommissionTwo().add(vo.getCommissionTwo()));
                vo.setCommissionOne(data.getCommissionOne().add(vo.getCommissionOne()));
                vo.setTotalAmount(data.getTotalAmount().add(vo.getTotalAmount()));
                vo.setTotalCount(data.getTotalCount() + vo.getTotalCount());
                if (StrUtil.isNotEmpty(data.getParentPath())) {
                    String merchantIdOne = data.getParentPath().split("/")[0];
                    MerchantEntity merchantOne = merchantMap.get(merchantIdOne);
                    if (merchantOne != null) {
                        data.setMerchantNameOne(merchantOne.getUserName());
                        data.setMerchantIdOne(merchantOne.getUserId());
                    }
                }
            });
            list.add(vo);
            if (SecurityUtils.hasRole(SysRoleEnum.MERCHANT.getRoleKey()) && dto.getMerchantLevel() != null) {
                list.forEach(data -> {
                    if (dto.getMerchantLevel() == 2) {
                        data.setCommissionOneRate(BigDecimal.ZERO);
                        data.setCommissionOne(BigDecimal.ZERO);
                    }else if (dto.getMerchantLevel() == 3) {
                        data.setCommissionOneRate(BigDecimal.ZERO);
                        data.setCommissionOne(BigDecimal.ZERO);
                        data.setCommissionTwoRate(BigDecimal.ZERO);
                        data.setCommissionTwo(BigDecimal.ZERO);
                    }else if (dto.getMerchantLevel() == 4) {
                        data.setCommissionOneRate(BigDecimal.ZERO);
                        data.setCommissionOne(BigDecimal.ZERO);
                        data.setCommissionTwoRate(BigDecimal.ZERO);
                        data.setCommissionTwo(BigDecimal.ZERO);
                        data.setCommissionThreeRate(BigDecimal.ZERO);
                        data.setCommissionThree(BigDecimal.ZERO);
                    }else if (dto.getMerchantLevel() == 5) {
                        data.setCommissionOneRate(BigDecimal.ZERO);
                        data.setCommissionOne(BigDecimal.ZERO);
                        data.setCommissionTwoRate(BigDecimal.ZERO);
                        data.setCommissionTwo(BigDecimal.ZERO);
                        data.setCommissionThreeRate(BigDecimal.ZERO);
                        data.setCommissionThree(BigDecimal.ZERO);
                        data.setCommissionFourRate(BigDecimal.ZERO);
                        data.setCommissionFour(BigDecimal.ZERO);
                    }
                });
            }
        }

        return list;
    }

    @Override
    public List<ReportForAgentVO> allMerchantReport(ReportForAgentDTO dto) {
        if (StrUtil.isEmpty(dto.getStartTime())) {
            dto.setStartTime(DateUtil.formatDateTime(DateUtil.beginOfDay(new Date())));
        } else {
            dto.setStartTime(DateUtil.formatDateTime(DateUtil.beginOfDay(DateUtil.parse(dto.getStartTime(), "yyyy-MM-dd"))));
        }
        if (StrUtil.isEmpty(dto.getEndTime())) {
            dto.setEndTime(DateUtil.formatDateTime(DateUtil.endOfDay(new Date())));
        } else {
            dto.setEndTime(DateUtil.formatDateTime(DateUtil.endOfDay(DateUtil.parse(dto.getEndTime(), "yyyy-MM-dd"))));
        }

        List<ReportForAgentVO> list = baseMapper.allMerchantReport(dto);
        if (list != null && !list.isEmpty()) {
            ReportForAgentVO vo = new ReportForAgentVO();
            vo.setUserName("合计");
            vo.setShopName("合计");
            Integer totalCount = 0,successCount = 0;
            BigDecimal totalMoney = BigDecimal.ZERO,successMoney= BigDecimal.ZERO,chargeMoney = BigDecimal.ZERO,
                    yesBalance= BigDecimal.ZERO,todayBalance = BigDecimal.ZERO,merchantFee = BigDecimal.ZERO;
            String startDate = DateUtil.format(DateUtil.offsetDay(DateUtil.parse(dto.getStartTime(), "yyyy-MM-dd HH:mm:ss"),-1), "yyyy-MM-dd");
            String endDate = DateUtil.format(DateUtil.parse(dto.getEndTime(), "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd");
            List<MerchantEntity> allMerchantList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantInfo), MerchantEntity.class);
            List<MerchantEntity> freezeList = merchantAmountRecordsMapper.getFreezeByUserDate(dto.getStartTime(), dto.getEndTime());
            Map<Long,BigDecimal> freezeMap = new HashMap<>();
            if (freezeList != null && !freezeList.isEmpty()) {
                freezeMap = freezeList.stream().collect(Collectors.toMap(MerchantEntity::getUserId, MerchantEntity::getFreezeAmount));
            }
            for (ReportForAgentVO data : list) {
                //如果结束日期是今天，则从redis中拿最新数据。否则从历史余额表中获取
                if (StrUtil.equals(endDate, DateUtil.format(new Date(), "yyyyMMdd"))) {
                    MerchantEntity merchant = BeanUtil.copyProperties(data, MerchantEntity.class);
                    BigDecimal todayBalanceRedis = BigDecimal.ZERO;
                    BigDecimal todayFreezeRedis = BigDecimal.ZERO;
                    //自己的余额
                    if (redisUtils.hasKey(RedisKeys.merchantBalance + merchant.getUserId())) {
                        Object balance = redisUtils.get(RedisKeys.merchantBalance + merchant.getUserId());
                        if (ObjectUtil.isNotEmpty(balance)) {
                            todayBalanceRedis = todayBalanceRedis.add(new BigDecimal(balance.toString()));
                        }
                    }
                    //自己的佣金
                    if (redisUtils.hasKey(RedisKeys.merchantCommission + merchant.getUserId())) {
                        Object commission = redisUtils.get(RedisKeys.merchantCommission + merchant.getUserId());
                        if (ObjectUtil.isNotEmpty(commission)) {
                            todayFreezeRedis = todayFreezeRedis.add(new BigDecimal(commission.toString()));
                        }
                    }
                    //下级的余额和佣金
                    List<MerchantEntity> child = merchantService.listAllChild(allMerchantList, merchant);
                    if (child != null && !child.isEmpty()) {
                        for (MerchantEntity childMerchent : child) {
                            //余额
                            if (redisUtils.hasKey(RedisKeys.merchantBalance + childMerchent.getUserId())) {
                                Object balance = redisUtils.get(RedisKeys.merchantBalance + childMerchent.getUserId());
                                if (ObjectUtil.isNotEmpty(balance)) {
                                    todayBalanceRedis = todayBalanceRedis.add(new BigDecimal(balance.toString()));
                                }
                            }
                            //余额
                            if (redisUtils.hasKey(RedisKeys.merchantCommission + childMerchent.getUserId())) {
                                Object commission = redisUtils.get(RedisKeys.merchantCommission + childMerchent.getUserId());
                                if (ObjectUtil.isNotEmpty(commission)) {
                                    todayFreezeRedis = todayFreezeRedis.add(new BigDecimal(commission.toString()));
                                }
                            }
                        }
                    }
                    data.setTodayBalance(todayBalanceRedis);
                    data.setTodayFreeze(todayFreezeRedis);
                } else {
                    MerchantHistoryBalanceEntity aa = historyBalanceMapper.totalBalanceByUserId(endDate, data.getUserId());
                    if (ObjectUtil.isNotEmpty(aa)) {
                        data.setTodayBalance(ObjectUtil.isNotEmpty(aa.getBalance()) ? aa.getBalance() : BigDecimal.ZERO);
                        data.setTodayFreeze(BigDecimal.ZERO);
                    } else {
                        data.setTodayBalance(BigDecimal.ZERO);
                        data.setTodayFreeze(BigDecimal.ZERO);
                    }
                }
                MerchantHistoryBalanceEntity aa = historyBalanceMapper.totalBalanceByUserId(startDate, data.getUserId());
                data.setYesBalance(ObjectUtil.isNotNull(aa) && ObjectUtil.isNotEmpty(aa.getBalance())?aa.getBalance():BigDecimal.ZERO);
                data.setYesFreeze(BigDecimal.ZERO);
                /*今日/昨日余额=今日/昨日余额+今日/昨日佣金*/
                data.setYesBalance(data.getYesBalance().add(data.getYesFreeze()));
                data.setTodayBalance(data.getTodayBalance().add(data.getTodayFreeze()));

                //统计日期内的佣金
                data.setMerchantFee(freezeMap.get(data.getUserId()));
                if (ObjectUtil.isNull(data.getMerchantFee())) {
                    data.setMerchantFee(BigDecimal.ZERO);
                }
                if (ObjectUtil.isNull(data.getSuccessMoney())) {
                    data.setSuccessMoney(BigDecimal.ZERO);
                }
                data.setChargeToMoney(data.getYesBalance().add(data.getChargeMoney()).add(data.getMerchantFee()).subtract(data.getSuccessMoney()).subtract(data.getTodayBalance()));
                //汇总
                if (ObjectUtil.isNotNull(data.getTotalCount())) {
                    totalCount +=  data.getTotalCount();
                }
                if (ObjectUtil.isNotNull(data.getSuccessCount())) {
                    successCount +=  data.getSuccessCount();
                }
                if (ObjectUtil.isNotNull(data.getTotalMoney())) {
                    totalMoney = totalMoney.add(data.getTotalMoney());
                }
                if (ObjectUtil.isNotNull(data.getSuccessMoney())) {
                    successMoney = successMoney.add(data.getSuccessMoney());
                }
                if (ObjectUtil.isNotNull(data.getYesBalance())) {
                    yesBalance = yesBalance.add(data.getYesBalance());
                }
                if (ObjectUtil.isNotNull(data.getTodayBalance())) {
                    todayBalance = todayBalance.add(data.getTodayBalance());
                }
                if (ObjectUtil.isNotNull(data.getMerchantFee())) {
                    merchantFee = merchantFee.add(data.getMerchantFee());
                }
                if (ObjectUtil.isNotNull(data.getChargeMoney())) {
                    chargeMoney = chargeMoney.add(data.getChargeMoney());
                }
                if (data.getTotalCount() != null && data.getSuccessCount() != null && data.getTotalCount() != 0) {
                    data.setSuccessRate(new BigDecimal(data.getSuccessCount()).divide(new BigDecimal(data.getTotalCount()),4, RoundingMode.HALF_UP));
                }
            }
            vo.setTotalCount(totalCount);
            vo.setSuccessCount(successCount);
            vo.setTotalMoney(totalMoney);
            vo.setSuccessMoney(successMoney);
            vo.setYesBalance(yesBalance);
            vo.setMerchantFee(merchantFee);
            vo.setTodayBalance(todayBalance);
            vo.setChargeMoney(chargeMoney);
            if (vo.getTotalCount() != null && vo.getSuccessCount() != null && vo.getTotalCount() != 0) {
                vo.setSuccessRate(new BigDecimal(vo.getSuccessCount()).divide(new BigDecimal(vo.getTotalCount()),4, RoundingMode.HALF_UP));
            }
            list.add(vo);
        }
        return list;
    }
}
