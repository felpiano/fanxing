package com.ruoyi.web.controller.business;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.enums.SysRoleEnum;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.business.MerchantAmountRecordsEntity;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.ruoyi.system.domain.business.ShopAmountRecordsEntity;
import com.ruoyi.system.domain.business.ShopMerchantRelationEntity;
import com.ruoyi.system.domain.dto.AmountChangeQueryDTO;
import com.ruoyi.system.domain.dto.ReportForAgentDTO;
import com.ruoyi.system.domain.vo.ReportForAgentVO;
import com.ruoyi.system.service.business.MerchantAmountRecordsService;
import com.ruoyi.system.service.business.MerchantService;
import com.ruoyi.system.service.business.ShopMerchantRelationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import com.ruoyi.common.core.controller.BaseController;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author admin
 * @since 2024-10-19
 */
@RestController
@Api("码商资金记录")
@RequestMapping("/merchantAmountRecordsEntity")
public class MerchantAmountRecordsController extends BaseController {

    @Resource
    private MerchantAmountRecordsService merchantAmountRecordsService;
    @Resource
    private ShopMerchantRelationService relationService;
    @Resource
    private MerchantService merchantService;
    @Autowired
    private RedisUtils redisUtils;

    @ApiOperation("获取码商帐变列表")
    @PreAuthorize("@ss.hasPermi('system:merchantCharge:list')")
    @PostMapping("/list")
    public R<Page<MerchantAmountRecordsEntity>> userList(@RequestBody AmountChangeQueryDTO queryDTO) {
        List<MerchantEntity> allList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantInfo), MerchantEntity.class);
        if (StrUtil.isNotEmpty(queryDTO.getParentPath())) {
            List<MerchantEntity> childList = allList.stream().filter(a -> a.getParentPath().startsWith(queryDTO.getParentPath())).collect(Collectors.toList());
            if (!childList.isEmpty()) {
                queryDTO.setParentIds(childList.stream().map(MerchantEntity::getUserId).collect(Collectors.toList()));
            }
        }
        Page<MerchantAmountRecordsEntity> rowPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        //queryWrapper组装查询where条件
        LambdaQueryWrapper<MerchantAmountRecordsEntity> queryWrapper = Wrappers.lambdaQuery(MerchantAmountRecordsEntity.class)
                .eq(ObjectUtil.isNotEmpty(queryDTO.getUserId()), MerchantAmountRecordsEntity::getUserId, queryDTO.getUserId())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getOrderNo()), MerchantAmountRecordsEntity::getOrderNo, queryDTO.getOrderNo())
                .like(StrUtil.isNotEmpty(queryDTO.getUserName()), MerchantAmountRecordsEntity::getUserName, queryDTO.getUserName())

                .eq(ObjectUtil.isNotEmpty(queryDTO.getAmountType()), MerchantAmountRecordsEntity::getAmountType, queryDTO.getAmountType())
                .ge(ObjectUtil.isNotEmpty(queryDTO.getMinAmount()), MerchantAmountRecordsEntity::getChangeAmount, queryDTO.getMinAmount())
                .le(ObjectUtil.isNotEmpty(queryDTO.getMaxAmount()), MerchantAmountRecordsEntity::getChangeAmount, queryDTO.getMaxAmount())
                .ge(ObjectUtil.isNotEmpty(queryDTO.getStartTime()), MerchantAmountRecordsEntity::getCreateTime, queryDTO.getStartTime())
                .le(ObjectUtil.isNotEmpty(queryDTO.getEndTime()), MerchantAmountRecordsEntity::getCreateTime, queryDTO.getEndTime())
                .in(ObjectUtil.isNotEmpty(queryDTO.getParentIds()), MerchantAmountRecordsEntity::getUserId, queryDTO.getParentIds());

        if (ObjectUtil.isNotEmpty(queryDTO.getChangeType())) {
            if (queryDTO.getChangeType() == 2) {
                queryWrapper.eq(MerchantAmountRecordsEntity::getChangeType, 2).gt(MerchantAmountRecordsEntity::getChangeAmount, 0);
            } else if (queryDTO.getChangeType() == 5) {
                queryWrapper.eq(MerchantAmountRecordsEntity::getChangeType, 2).lt(MerchantAmountRecordsEntity::getChangeAmount, 0);
            } else {
                queryWrapper.eq(MerchantAmountRecordsEntity::getChangeType, queryDTO.getChangeType());
            }
        }

        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        if (sysUser != null) {
            if (sysUser.getIdentity() == 3){
                queryWrapper.eq(MerchantAmountRecordsEntity::getAgentId, sysUser.getUserId());
            }else if (sysUser.getIdentity() == 5){
                MerchantEntity merchant = merchantService.getById(sysUser.getUserId());
                if (merchant != null && merchant.getMerchantLevel() != 1) {
                    queryWrapper.eq(MerchantAmountRecordsEntity::getUserId, sysUser.getUserId());
                } else if (merchant != null && StrUtil.isEmpty(queryDTO.getParentPath())) {
                    List<MerchantEntity> childList = allList.stream().filter(a -> a.getParentPath().startsWith(merchant.getParentPath())).collect(Collectors.toList());
                    queryWrapper.in(MerchantAmountRecordsEntity::getUserId, childList.stream().map(MerchantEntity::getUserId).collect(Collectors.toList()));
                }
            } else if (sysUser.getIdentity() == 4){
                List<ShopMerchantRelationEntity> list = relationService.list(
                        Wrappers.lambdaQuery(ShopMerchantRelationEntity.class)
                                .eq(ShopMerchantRelationEntity::getShopId, sysUser.getUserId())
                );
                if (list != null && !list.isEmpty()) {
                    queryWrapper.in(MerchantAmountRecordsEntity::getUserId, list.stream().map(ShopMerchantRelationEntity::getMerchantId).collect(Collectors.toList()));
                } else {
                    queryWrapper.eq(MerchantAmountRecordsEntity::getUserId, sysUser.getUserId());
                }
            }
            queryWrapper.orderByDesc(MerchantAmountRecordsEntity::getId);
            return R.ok(merchantAmountRecordsService.page(rowPage, queryWrapper));
        }
        return R.ok(new Page<>());
    }

    @ApiOperation("码商帐变导出")
    @PreAuthorize("@ss.hasPermi('system:merchantCharge:list')")
    @PostMapping("/recordsExport")
    public void recordsExport(@RequestBody AmountChangeQueryDTO queryDTO, HttpServletResponse response) throws IOException {
        if (StrUtil.isNotEmpty(queryDTO.getParentPath())) {
            List<MerchantEntity> allList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantInfo), MerchantEntity.class);
            List<MerchantEntity> childList = allList.stream().filter(a -> a.getParentPath().startsWith(queryDTO.getParentPath())).collect(Collectors.toList());
            if (childList!= null && !childList.isEmpty()) {
                queryDTO.setParentIds(childList.stream().map(MerchantEntity::getUserId).collect(Collectors.toList()));
            }
        }
        LambdaQueryWrapper<MerchantAmountRecordsEntity> queryWrapper = Wrappers.lambdaQuery(MerchantAmountRecordsEntity.class)
                .eq(ObjectUtil.isNotEmpty(queryDTO.getUserId()), MerchantAmountRecordsEntity::getUserId, queryDTO.getUserId())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getOrderNo()), MerchantAmountRecordsEntity::getOrderNo, queryDTO.getOrderNo())
                .like(StrUtil.isNotEmpty(queryDTO.getUserName()), MerchantAmountRecordsEntity::getUserName, queryDTO.getUserName())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getChangeType()), MerchantAmountRecordsEntity::getChangeType, queryDTO.getChangeType())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getAmountType()), MerchantAmountRecordsEntity::getAmountType, queryDTO.getAmountType())
                .ge(ObjectUtil.isNotEmpty(queryDTO.getMinAmount()), MerchantAmountRecordsEntity::getChangeAmount, queryDTO.getMinAmount())
                .le(ObjectUtil.isNotEmpty(queryDTO.getMaxAmount()), MerchantAmountRecordsEntity::getChangeAmount, queryDTO.getMaxAmount())
                .ge(ObjectUtil.isNotEmpty(queryDTO.getStartTime()), MerchantAmountRecordsEntity::getCreateTime, queryDTO.getStartTime())
                .le(ObjectUtil.isNotEmpty(queryDTO.getEndTime()), MerchantAmountRecordsEntity::getCreateTime, queryDTO.getEndTime())
                .in(ObjectUtil.isNotEmpty(queryDTO.getParentIds()), MerchantAmountRecordsEntity::getUserId, queryDTO.getParentIds())
                .orderByDesc(MerchantAmountRecordsEntity::getId);
        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        if (sysUser.getIdentity() == 3){
            queryWrapper.eq(MerchantAmountRecordsEntity::getAgentId, sysUser.getUserId());
        }else if (sysUser.getIdentity() == 5){
            queryWrapper.eq(MerchantAmountRecordsEntity::getUserId, sysUser.getUserId());
        } else if (sysUser.getIdentity() == 4){
            List<ShopMerchantRelationEntity> list = relationService.list(
                    Wrappers.lambdaQuery(ShopMerchantRelationEntity.class)
                            .eq(ShopMerchantRelationEntity::getShopId, sysUser.getUserId())
            );
            if (list != null && !list.isEmpty()) {
                queryWrapper.in(MerchantAmountRecordsEntity::getUserId, list.stream().map(ShopMerchantRelationEntity::getMerchantId).collect(Collectors.toList()));
            } else {
                queryWrapper.eq(MerchantAmountRecordsEntity::getUserId, sysUser.getUserId());
            }
        }
        List<MerchantAmountRecordsEntity> exportList = merchantAmountRecordsService.list(queryWrapper);
        exportAmountRecords(response, exportList);
    }

    @ApiOperation("获取码商帐变列表")
    @PreAuthorize("@ss.hasPermi('system:merchantCharge:list')")
    @PostMapping("/oneMerchantList")
    public R<Page<MerchantAmountRecordsEntity>> oneMerchantList(@RequestBody AmountChangeQueryDTO queryDTO) {
        if (StrUtil.isEmpty(queryDTO.getParentPath())) {
            MerchantEntity merchantEntity = merchantService.getById(SecurityUtils.getUserId());
            if (merchantEntity != null) {
                queryDTO.setParentPath(merchantEntity.getParentPath());
            }
        }
        Page<MerchantAmountRecordsEntity> rowPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        return R.ok(merchantAmountRecordsService.amountRecords(rowPage, queryDTO));
    }

    @ApiOperation("码商帐变导出")
    @PreAuthorize("@ss.hasPermi('system:merchantCharge:list')")
    @PostMapping("/oneMerchantExport")
    public void oneMerchantExport(@RequestBody AmountChangeQueryDTO queryDTO, HttpServletResponse response) throws IOException {
        if (StrUtil.isEmpty(queryDTO.getParentPath())) {
            MerchantEntity merchantEntity = merchantService.getById(SecurityUtils.getUserId());
            if (merchantEntity != null) {
                queryDTO.setParentPath(merchantEntity.getParentPath());
            }
        }
        List<MerchantAmountRecordsEntity> exportList = merchantAmountRecordsService.amountRecords(queryDTO);
        exportAmountRecords(response, exportList);
    }

    private void exportAmountRecords(HttpServletResponse response, List<MerchantAmountRecordsEntity> exportList) throws IOException {
        if (!exportList.isEmpty()) {
            exportList.forEach(data -> {
                if (1 == data.getAmountType()) {
                    data.setAmountTypeStr("余额");
                }else if (2 == data.getAmountType()) {
                    data.setAmountTypeStr("佣金");
                }
                if (1 == data.getChangeType()) {
                    data.setChangeTypeStr("系统充值");
                }else if (2 == data.getChangeType()) {
                    if (ObjectUtil.isNotEmpty(data.getChangeAmount())) {
                        if (data.getChangeAmount().compareTo(BigDecimal.ZERO) >= 0) {
                            data.setChangeTypeStr("管理员上分");
                        } else {
                            data.setChangeTypeStr("管理员扣款");
                        }
                    } else {
                        data.setChangeTypeStr("管理员上分");
                    }

                }else if (3 == data.getChangeType()) {
                    data.setChangeTypeStr("余额转移/佣金转移");
                } else if (4 == data.getChangeType()) {
                    data.setChangeTypeStr("订单冲正");
                }
                data.setCreateTimeStr(DateUtil.formatDateTime(data.getCreateTime()));
            });
        }
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.addHeaderAlias("userId","码商ID");
        writer.setColumnWidth(0, 10);
        writer.addHeaderAlias("userName","码商名称");
        writer.setColumnWidth(1, 10);
        writer.addHeaderAlias("amountTypeStr","金额类型");
        writer.setColumnWidth(2, 10);
        writer.addHeaderAlias("changeTypeStr","变更类型");
        writer.setColumnWidth(3, 10);
        writer.addHeaderAlias("beforeAmount","变更前金额");
        writer.setColumnWidth(4, 10);
        writer.addHeaderAlias("afterAmount","变更后金额");
        writer.setColumnWidth(5, 10);
        writer.addHeaderAlias("changeAmount","变更金额");
        writer.setColumnWidth(6, 10);
        writer.addHeaderAlias("createTimeStr","变更时间");
        writer.setColumnWidth(7, 20);
        writer.addHeaderAlias("orderNo","订单号");
        writer.setColumnWidth(8, 20);
        writer.addHeaderAlias("remarks","备注");
        writer.setColumnWidth(9, 80);
        writer.setOnlyAlias(true);
        writer.write(exportList, true);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName= URLEncoder.encode("码商资金明细" + DateUtil.format(new Date(),"yyyyMMddHHmmss"),"UTF-8");
        response.setHeader("Content-Disposition","attachment;filename="+fileName+".xlsx");
        ServletOutputStream out=response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        IoUtil.close(out);
    }
}
