package com.ruoyi.system.service.business.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.system.domain.business.MerchantChannelEntity;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.ruoyi.system.domain.business.MerchantQrcodeEntity;
import com.ruoyi.system.domain.dto.MerchantQrcodeOrderDTO;
import com.ruoyi.system.domain.dto.MerchantQrcodeQueryDTO;
import com.ruoyi.system.domain.dto.MerchantQrcodeSaveDTO;
import com.ruoyi.system.domain.vo.MerchantQrcodeReportVO;
import com.ruoyi.system.mapper.business.MerchantQrcodeMapper;
import com.ruoyi.system.service.business.MerchantChannelService;
import com.ruoyi.system.service.business.MerchantQrcodeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.system.service.business.MerchantService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 码商上码 服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-10-20
 */
@Service
public class MerchantQrcodeServiceImp extends ServiceImpl<MerchantQrcodeMapper, MerchantQrcodeEntity> implements MerchantQrcodeService {

    @Resource
    private MerchantService merchantService;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private MerchantChannelService merchantChannelService;
    @Resource
    private AsyncRedisService asyncRedisService;

    @Override
    public AjaxResult saveMerchantQrcode(MerchantQrcodeSaveDTO saveDTO) {
        if (saveDTO.getQrcodeType() == 0 && StrUtil.isEmpty(saveDTO.getQrcodeValue())) {
            return AjaxResult.error("解析地址不能为空");
        }
        if (saveDTO.getQrcodeType() == 1 && StrUtil.isEmpty(saveDTO.getQrcodeUrl())) {
            return AjaxResult.error("图片为空，请重新上传");
        }
        List<MerchantQrcodeEntity> hasAccount = new ArrayList<>();
        if (saveDTO.getId() != null && StrUtil.isNotEmpty(saveDTO.getAccountNumber())) {
             hasAccount = baseMapper.selectList(Wrappers.lambdaQuery(MerchantQrcodeEntity.class)
                             .ne(MerchantQrcodeEntity::getId, saveDTO.getId())
                             .eq(MerchantQrcodeEntity::getChannelId, saveDTO.getChannelId())
                            .eq(MerchantQrcodeEntity::getAccountNumber, saveDTO.getAccountNumber()));
        } else if (StrUtil.isNotEmpty(saveDTO.getAccountNumber())){
            hasAccount = baseMapper.selectList(Wrappers.lambdaQuery(MerchantQrcodeEntity.class)
                    .eq(MerchantQrcodeEntity::getChannelId, saveDTO.getChannelId())
                    .eq(MerchantQrcodeEntity::getAccountNumber, saveDTO.getAccountNumber()));
        }
        if (hasAccount != null && !hasAccount.isEmpty()) {
            return AjaxResult.error("账号已存在，请勿重复上码");
        }
        MerchantEntity merchant = merchantService.getById(SecurityUtils.getUserId());
        if (StrUtil.isEmpty(merchant.getSafeCode()) || SecurityUtils.matchesPassword(saveDTO.getSafeCode(), merchant.getSafeCode())) {
            merchant.setSafeCode(SecurityUtils.encryptPassword(saveDTO.getSafeCode()));
            merchantService.updateById(merchant);
            //获取码商通道
            MerchantChannelEntity merchantChannel = merchantChannelService.getOne(Wrappers.lambdaQuery(MerchantChannelEntity.class)
                    .eq(MerchantChannelEntity::getChannelId, saveDTO.getChannelId())
                    .eq(MerchantChannelEntity::getMerchantId, saveDTO.getMerchantId()));
            MerchantQrcodeEntity saveEntity = BeanUtil.copyProperties(saveDTO, MerchantQrcodeEntity.class);
            if (ObjectUtil.isEmpty(saveDTO.getId())) {
                if (saveDTO.getQrcodeType() == 1) {
                    //解析图片
                    try {
                        saveEntity.setQrcodeValue(qrcodeAnalysis(saveEntity.getQrcodeUrl()));
                    }catch (ServiceException e){
                        return AjaxResult.error(e.getMessage());
                    }
                    if (StrUtil.isEmpty(saveEntity.getQrcodeValue())) {
                        return AjaxResult.error("请上传二维码");
                    }
                }
                saveEntity.setMerchantChannelId(merchantChannel.getId());
                saveEntity.setMerchantId(merchant.getUserId());
                saveEntity.setAgentId(merchant.getAgentId());
                this.save(saveEntity);
                //初始化该码的订单金额和笔数
                redisUtils.set(RedisKeys.merchantQrcodeLimitAmount + saveEntity.getId(), "0");
                redisUtils.set(RedisKeys.merchantQrcodeLimitCount + saveEntity.getId(), "0");
            } else {
                if (saveDTO.getQrcodeType() == 1) {
                    //解析图片
                    try {
                        saveEntity.setQrcodeValue(qrcodeAnalysis(saveEntity.getQrcodeUrl()));
                    }catch (ServiceException e){
                        return AjaxResult.error(e.getMessage());
                    }
                    if (StrUtil.isEmpty(saveEntity.getQrcodeValue())) {
                        return AjaxResult.error("请上传二维码");
                    }
                }
                this.updateById(saveEntity);
            }
            asyncRedisService.asyncReportQrcode();
            return AjaxResult.success();
        }
        return AjaxResult.error("安全码错误");
    }

    private String qrcodeAnalysis(String address){
        InputStream inputStream = null;
        try {
            URL url = new URL(address);
            inputStream = url.openConnection().getInputStream();
            return QrCodeUtil.decode(inputStream);
        } catch (Exception e) {
            log.error("图片获取异常");
            throw new ServiceException("请上传二维码");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("图片获取异常");
                }
            }
        }
    }

    @Override
    public MerchantQrcodeReportVO reportQrcode(MerchantQrcodeOrderDTO dto) {
        return baseMapper.reportQrcode(dto);
    }

    @Override
    public Page<MerchantQrcodeEntity> totalChildQrcode(MerchantQrcodeQueryDTO queryDTO) {
        Page<MerchantQrcodeEntity> rowpage = new Page(queryDTO.getPageNum(), queryDTO.getPageSize());
        MerchantEntity merchant = merchantService.getById(SecurityUtils.getUserId());
        queryDTO.setParentPath(merchant.getParentPath());
        return baseMapper.totalChildQrcode(rowpage, queryDTO);
    }
}
