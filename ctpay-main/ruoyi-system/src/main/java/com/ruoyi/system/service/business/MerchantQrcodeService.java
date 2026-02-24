package com.ruoyi.system.service.business;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.business.MerchantQrcodeEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.dto.MerchantQrcodeOrderDTO;
import com.ruoyi.system.domain.dto.MerchantQrcodeQueryDTO;
import com.ruoyi.system.domain.dto.MerchantQrcodeSaveDTO;
import com.ruoyi.system.domain.vo.MerchantQrcodeReportVO;

/**
 * <p>
 * 码商上码 服务类
 * </p>
 *
 * @author admin
 * @since 2024-10-20
 */
public interface MerchantQrcodeService extends IService<MerchantQrcodeEntity> {

    AjaxResult saveMerchantQrcode(MerchantQrcodeSaveDTO saveDTO);

    MerchantQrcodeReportVO reportQrcode(MerchantQrcodeOrderDTO dto);

    Page<MerchantQrcodeEntity> totalChildQrcode(MerchantQrcodeQueryDTO queryDTO);
}
