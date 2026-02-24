package com.ruoyi.system.mapper.business;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.system.domain.business.MerchantAmountRecordsEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.ruoyi.system.domain.dto.AmountChangeQueryDTO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
public interface MerchantAmountRecordsMapper extends BaseMapper<MerchantAmountRecordsEntity> {
    BigDecimal getMoneyByUserId(@Param("userId") Long userId, @Param("changeType")Integer changeType,@Param("startTime") String startTime, @Param("endTime")String endTime);

    List<MerchantEntity> getFreezeByDate(@Param("startTime") String startTime, @Param("endTime") String endTime);

    List<MerchantEntity> getFreezeByUserDate(@Param("startTime") String startTime, @Param("endTime") String endTime);

    List<MerchantAmountRecordsEntity> getMoneyGroupByUserId(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 资金明细
     * @param dto
     * @return
     */
    Page<MerchantAmountRecordsEntity> amountRecords(Page page, @Param("dto") AmountChangeQueryDTO dto);

    List<MerchantAmountRecordsEntity> amountRecords(@Param("dto") AmountChangeQueryDTO dto);
}
