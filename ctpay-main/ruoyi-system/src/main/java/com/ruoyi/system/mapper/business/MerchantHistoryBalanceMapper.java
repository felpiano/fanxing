package com.ruoyi.system.mapper.business;

import com.ruoyi.system.domain.business.MerchantHistoryBalanceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 码商每日余额历史 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2024-11-06
 */
public interface MerchantHistoryBalanceMapper extends BaseMapper<MerchantHistoryBalanceEntity> {
    MerchantHistoryBalanceEntity totalBalanceByParentPath(@Param("dataDt") String dataDt, @Param("parentPath")String parentPath);

    List<MerchantHistoryBalanceEntity> init(@Param("endDate")String endDate, @Param("dataDt")String dataDt);

    MerchantHistoryBalanceEntity totalBalanceByUserId(@Param("dataDt") String dataDt, @Param("userId")Long userId);

    List<MerchantHistoryBalanceEntity> totalBalanceBroupByUserId(@Param("dataDt") String dataDt);

    List<MerchantHistoryBalanceEntity> balanceBroupByUserId(@Param("dataDt") String dataDt);
}
