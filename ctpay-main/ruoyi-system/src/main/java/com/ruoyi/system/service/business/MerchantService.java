package com.ruoyi.system.service.business;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.business.InOrderDetailEntity;
import com.ruoyi.system.domain.business.InOrderEntity;
import com.ruoyi.system.domain.business.MerchantChannelEntity;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.dto.AmountChangeDTO;
import com.ruoyi.system.domain.vo.MerchantDepositVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 码商 服务类
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
public interface MerchantService extends IService<MerchantEntity> {

    AjaxResult addMerchant(MerchantEntity merchant);

    AjaxResult updateMerchant(MerchantEntity merchant);

    AjaxResult deleteMerchant(Long merchantId);

    /**
     * 删除码商及其下级
     * @param merchantId
     * @return
     */
    AjaxResult  deleteMerchantChild(Long merchantId);

    /**
     * 修改余额
     *
     * @param amountChangeDTO
     */
    void updateAmount(AmountChangeDTO amountChangeDTO);


    /**
     * 订单扣减金额
     *
     * @param amountChangeDTO
     */
    void subAmount(AmountChangeDTO amountChangeDTO);

    /**
     * 押金阈值
     * @return
     */
    List<MerchantDepositVO> listBaseDeposit();

    /**
     * 获取上下级
     * @return
     */
    List<MerchantEntity> getParentlist(MerchantEntity merchantEntity);


    /**
     * 所有下级码商
     * @param allList
     * @param merchantEntity
     * @return
     */
    List<MerchantEntity> listAllChild(List<MerchantEntity> allList, MerchantEntity merchantEntity);

    /**
     * 划转佣金至余额
     * @param merchant
     * @param amount
     */
    void trimFreezeToBalance(MerchantEntity merchant, BigDecimal amount, String remarks);

    /**
     * 订单增加佣金
     * @param inOrderEntity
     * @param detailEntity
     */
    void addMerchantCommission(InOrderEntity inOrderEntity, InOrderDetailEntity detailEntity);

    /**
     * 根据代理查询1级码商以下的码商通道
     * @return
     */
    AjaxResult onekeyClearRate(Long channelId);

    /**
     * 订单冲正，扣减佣金
     * @param tradeNo
     */
    void subMerchantCommission(String tradeNo);

    /**
     *  把余额转给其他码商
     * @param merchantUserEntity  当前登录用户实体类
     * @param merchantEntity 转移码商信息
     * @param changeAmount 改变金额
     * @param remark 备注
     */
    void updateAmountToMerchant(MerchantEntity merchantUserEntity, MerchantEntity merchantEntity, BigDecimal changeAmount, String remark);
}
