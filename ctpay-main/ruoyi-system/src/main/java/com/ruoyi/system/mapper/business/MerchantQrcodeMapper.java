package com.ruoyi.system.mapper.business;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.system.domain.business.MerchantQrcodeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.dto.MerchantQrcodeOrderDTO;
import com.ruoyi.system.domain.dto.MerchantQrcodeQueryDTO;
import com.ruoyi.system.domain.vo.MerchantQrcodeReportVO;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 码商上码 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2024-10-20
 */
public interface MerchantQrcodeMapper extends BaseMapper<MerchantQrcodeEntity> {
    MerchantQrcodeReportVO reportQrcode(@Param("dto") MerchantQrcodeOrderDTO dto);

    Page<MerchantQrcodeEntity> totalChildQrcode(@Param("page") Page<MerchantQrcodeEntity> page, @Param("dto") MerchantQrcodeQueryDTO dto);
}
