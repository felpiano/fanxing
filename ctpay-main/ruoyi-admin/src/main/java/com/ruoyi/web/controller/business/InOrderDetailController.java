package com.ruoyi.web.controller.business;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.enums.OrderStatus;
import com.ruoyi.common.enums.SysRoleEnum;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.business.InOrderDetailEntity;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.ruoyi.system.domain.dto.OrderQueryDTO;
import com.ruoyi.system.domain.vo.OrderTotalVO;
import com.ruoyi.system.domain.vo.OrderVO;
import com.ruoyi.system.service.business.InOrderDetailService;
import com.ruoyi.system.service.business.MerchantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单副表 前端控制器
 * </p>
 *
 * @author admin
 * @since 2024-10-31
 */
@RestController
@RequestMapping("/inOrderDetailEntity")
@Api("订单查询与统计")
public class InOrderDetailController extends BaseController {

    @Resource
    private InOrderDetailService detailService;
    @Resource
    private MerchantService merchantService;
    @Resource
    private RedisUtils redisUtils;

    @GetMapping("/getById")
    @ApiOperation("根据ID获取订单详情")
    @PreAuthorize("@ss.hasPermi('system:order:detail')")
    public R<InOrderDetailEntity> getById(@RequestParam("id") Long id) {
        return R.ok(detailService.getById(id));
    }

    @ApiOperation("获取订单列表")
    @PreAuthorize("@ss.hasPermi('system:order:list')")
    @PostMapping("/list")
    public R<Page<OrderVO>> list(@RequestBody OrderQueryDTO queryDTO) {
        Page<OrderVO> rowPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        SysUser user = SecurityUtils.getLoginUser().getUser();
        MerchantEntity merchant = merchantService.getById(user.getUserId());
        if (!SecurityUtils.hasRole(SysRoleEnum.ADMIN.getRoleKey())) {
            if (SecurityUtils.hasRole(SysRoleEnum.SHOP.getRoleKey())) {
                queryDTO.setShopId(user.getUserId());
            } else if (SecurityUtils.hasRole(SysRoleEnum.AGENT.getRoleKey())) {
                queryDTO.setAgentId(user.getUserId());
            } else if (SecurityUtils.hasRole(SysRoleEnum.MERCHANT.getRoleKey())) {
                if (merchant != null) {
                    queryDTO.setSelftMerchantId(merchant.getUserId());
                    List<MerchantEntity> childList = merchantService.list(Wrappers.lambdaQuery(MerchantEntity.class)
                            .likeRight(MerchantEntity::getParentPath, merchant.getParentPath())
                            .ne(MerchantEntity::getUserId, user.getUserId()));
                    if (!childList.isEmpty()) {
                        queryDTO.setChildIds(childList.stream().map(MerchantEntity::getUserId).collect(Collectors.toList()));
                    } else {
                        return R.ok(new Page<>(rowPage.getCurrent(), rowPage.getSize(), rowPage.getTotal()));
                    }
                }
            }
        }
        if (StrUtil.isNotEmpty(queryDTO.getParentMerchantName())) {
            MerchantEntity merchantEntity = merchantService.getOne(Wrappers.lambdaQuery(MerchantEntity.class)
                    .eq(MerchantEntity::getMerchantLevel, 1)
                    .eq(MerchantEntity::getMerchantName, queryDTO.getParentMerchantName()));
            if (merchantEntity != null) {
                queryDTO.setParentMerchantName(merchantEntity.getParentPath());
            } else {
                queryDTO.setParentMerchantName("~");
            }
        }
        Page<OrderVO> page = detailService.queryOrderPage(rowPage, queryDTO);
        if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            //获取所有码商
            List<MerchantEntity> allList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantInfo), MerchantEntity.class);
            //当前码商余额
            List<Object> balanceKeys = new ArrayList<>();
            List<Object> commissionKeys = new ArrayList<>();
            page.getRecords().forEach(data -> {
                balanceKeys.add(RedisKeys.merchantBalance + data.getMerchantId());
                commissionKeys.add(RedisKeys.merchantCommission + data.getMerchantId());
            });
            Map<Object, Object> balanceObject = redisUtils.batchQueryKeysAndValues(balanceKeys);
            Map<Object, Object> commissionObject = redisUtils.batchQueryKeysAndValues(commissionKeys);

            page.getRecords().forEach(data -> {
                if (StrUtil.isNotEmpty(data.getClientIp()) && redisUtils.hasKey(RedisKeys.clientIpList + data.getClientIp())) {
                    data.setShowPusBlack(1);
                } else {
                    data.setShowPusBlack(0);
                }
                if (ObjectUtil.isNotEmpty(balanceObject.get(RedisKeys.merchantBalance + data.getMerchantId()))) {
                    data.setMerchantBalance(balanceObject.get(RedisKeys.merchantBalance + data.getMerchantId()).toString());
                } else {
                    data.setMerchantBalance("0");
                }
                if (ObjectUtil.isNotEmpty(commissionObject.get(RedisKeys.merchantCommission + data.getMerchantId()))) {
                    data.setMerchantFreeze(commissionObject.get(RedisKeys.merchantCommission + data.getMerchantId()).toString());
                } else {
                    data.setMerchantFreeze("0");
                }
                data.setFirstMerchantFee(data.getOrderAmount().multiply(data.getMerchantRateOne()).divide(new BigDecimal(100), 2 , RoundingMode.HALF_UP));
                if (merchant != null && SecurityUtils.hasRole(SysRoleEnum.MERCHANT.getRoleKey())) {
                    data.setMyMerchantLevel(merchant.getMerchantLevel());
                    switch (merchant.getMerchantLevel()) {
                        case 1:
                            data.setMyFee(data.getOrderAmount().multiply(data.getMerchantRateOne()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                            data.setChildFee(data.getOrderAmount().multiply(data.getMerchantRateTwo()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                            break;
                        case 2:
                            data.setMyFee(data.getOrderAmount().multiply(data.getMerchantRateTwo()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                            data.setChildFee(data.getOrderAmount().multiply(data.getMerchantRateThree()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                            break;
                        case 3:
                            data.setMyFee(data.getOrderAmount().multiply(data.getMerchantRateThree()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                            data.setChildFee(data.getOrderAmount().multiply(data.getMerchantRateFour()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                            break;
                        case 4:
                            data.setMyFee(data.getOrderAmount().multiply(data.getMerchantRateFour()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                            data.setChildFee(data.getOrderAmount().multiply(data.getMerchantRateFive()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                            break;
                        case 5:
                            data.setMyFee(data.getOrderAmount().multiply(data.getMerchantRateFive()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                            data.setChildFee(BigDecimal.ZERO);
                            break;
                    }
                }
                if (StrUtil.isNotEmpty(data.getParentPath())) {
                    String firstUserId = data.getParentPath().split("/")[0];
                    MerchantEntity first = allList.stream().filter(a -> firstUserId.equals(a.getUserId().toString())).findFirst().orElse(null);
                    if (first != null) {
                        data.setFirstUserId(first.getUserId());
                        data.setFirstUserName(first.getUserName());
                    }
                }
            });
        }
        return R.ok(page);
    }

    @ApiOperation("获取订单列表汇总")
    @PreAuthorize("@ss.hasPermi('system:order:list')")
    @PostMapping("/listTotal")
    public R<OrderTotalVO> listTotal(@RequestBody OrderQueryDTO queryDTO) {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        MerchantEntity merchant = merchantService.getById(user.getUserId());
        if (!SecurityUtils.hasRole(SysRoleEnum.ADMIN.getRoleKey())) {
            if (SecurityUtils.hasRole(SysRoleEnum.SHOP.getRoleKey())) {
                queryDTO.setShopId(user.getUserId());
            } else if (SecurityUtils.hasRole(SysRoleEnum.AGENT.getRoleKey())) {
                queryDTO.setAgentId(user.getUserId());
            } else if (SecurityUtils.hasRole(SysRoleEnum.MERCHANT.getRoleKey())) {
                if (merchant != null) {
                    queryDTO.setSelftMerchantId(merchant.getUserId());
                    List<MerchantEntity> childList = merchantService.list(Wrappers.lambdaQuery(MerchantEntity.class)
                            .likeRight(MerchantEntity::getParentPath, merchant.getParentPath())
                            .ne(MerchantEntity::getUserId, user.getUserId()));
                    if (!childList.isEmpty()) {
                        queryDTO.setChildIds(childList.stream().map(MerchantEntity::getUserId).collect(Collectors.toList()));
                    }
                }
            }
        }
        if (StrUtil.isNotEmpty(queryDTO.getParentMerchantName())) {
            MerchantEntity merchantEntity = merchantService.getOne(Wrappers.lambdaQuery(MerchantEntity.class)
                    .eq(MerchantEntity::getMerchantLevel, 1)
                    .eq(MerchantEntity::getMerchantName, queryDTO.getParentMerchantName()));
            if (merchantEntity != null) {
                queryDTO.setParentMerchantName(merchantEntity.getParentPath());
            } else {
                queryDTO.setParentMerchantName("~");
            }
        }
        return R.ok(detailService.queryOrderListTotal(queryDTO));
    }

    @ApiOperation("订单导出")
    @PreAuthorize("@ss.hasPermi('system:order:list')")
    @PostMapping("/orderExport")
    public void orderExport(@RequestBody OrderQueryDTO queryDTO, HttpServletResponse response) throws IOException {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        if (!SecurityUtils.hasRole(SysRoleEnum.ADMIN.getRoleKey())) {
            if (SecurityUtils.hasRole(SysRoleEnum.SHOP.getRoleKey())) {
                queryDTO.setShopId(user.getUserId());
            } else if (SecurityUtils.hasRole(SysRoleEnum.AGENT.getRoleKey())) {
                queryDTO.setAgentId(user.getUserId());
            } else if (SecurityUtils.hasRole(SysRoleEnum.MERCHANT.getRoleKey())) {
                MerchantEntity merchant = merchantService.getById(user.getUserId());
                if (merchant != null) {
                    queryDTO.setSelftMerchantId(merchant.getUserId());
                    List<MerchantEntity> childList = merchantService.list(Wrappers.lambdaQuery(MerchantEntity.class)
                            .likeRight(MerchantEntity::getParentPath, merchant.getParentPath())
                            .ne(MerchantEntity::getUserId, user.getUserId()));
                    if (!childList.isEmpty()) {
                        queryDTO.setChildIds(childList.stream().map(MerchantEntity::getUserId).collect(Collectors.toList()));
                    }
                }
            }
        }
        if (StrUtil.isNotEmpty(queryDTO.getParentMerchantName())) {
            MerchantEntity merchantEntity = merchantService.getOne(Wrappers.lambdaQuery(MerchantEntity.class)
                    .eq(MerchantEntity::getMerchantLevel, 1)
                    .eq(MerchantEntity::getMerchantName, queryDTO.getParentMerchantName()));
            if (merchantEntity != null) {
                queryDTO.setParentMerchantName(merchantEntity.getParentPath());
            } else {
                queryDTO.setParentMerchantName("~");
            }
        }
        List<OrderVO> exportList = detailService.queryOrderList( queryDTO);
        if (!exportList.isEmpty()) {
            //获取所有码商
            List<MerchantEntity> allList = JSONUtil.toList(redisUtils.get(RedisKeys.merchantInfo), MerchantEntity.class);
            exportList.forEach(data -> {
                data.setFirstMerchantFee(data.getOrderAmount().multiply(data.getMerchantRateOne()).divide(new BigDecimal(100), 2 , RoundingMode.HALF_UP));
                if (StrUtil.isNotEmpty(data.getParentPath())) {
                    String firstUserId = data.getParentPath().split("/")[0];
                    MerchantEntity first = allList.stream().filter(a -> firstUserId.equals(a.getUserId().toString())).findFirst().orElse(null);
                    if (first != null) {
                        data.setFirstUserId(first.getUserId());
                        data.setFirstUserName(first.getUserName());
                    }
                }
                if (OrderStatus.WAIT.getValue() == data.getOrderStatus()) {
                    data.setOrderStatusStr("待支付");
                }else if (OrderStatus.FINISH.getValue() == data.getOrderStatus()) {
                    data.setOrderStatusStr("支付成功");
                }else if (OrderStatus.TIMEOUT.getValue() == data.getOrderStatus()) {
                    data.setOrderStatusStr("订单超时");
                }else if (OrderStatus.CLOSED.getValue() == data.getOrderStatus()) {
                    data.setOrderStatusStr("已关闭");
                }else if (OrderStatus.BACKING.getValue() == data.getOrderStatus()) {
                    data.setOrderStatusStr("待退回");
                }else if (OrderStatus.BACKED.getValue() == data.getOrderStatus()) {
                    data.setOrderStatusStr("已退回");
                }
                if (0 == data.getCallbackStatus()) {
                    data.setCallbackStatusStr("待回调");
                }else if (1 == data.getCallbackStatus()) {
                    data.setCallbackStatusStr("已回调");
                }
                data.setAccountInfo("收款人：" + data.getNickName() + ";收款账号：" + data.getAccountNumber());
            });
        }
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.addHeaderAlias("shopName","商户名称");
        writer.setColumnWidth(0, 10);
        writer.addHeaderAlias("firstUserName","一级码商");
        writer.setColumnWidth(1, 10);
        writer.addHeaderAlias("merchantName","码商名称");
        writer.setColumnWidth(2, 10);
        writer.addHeaderAlias("channelName","通道名称");
        writer.setColumnWidth(3, 15);
        writer.addHeaderAlias("tradeNo","平台单号");
        writer.setColumnWidth(4, 25);
        writer.addHeaderAlias("shopOrderNo","商户单号");
        writer.setColumnWidth(5, 25);
        writer.addHeaderAlias("payer","支付用户");
        writer.setColumnWidth(6, 10);
        writer.addHeaderAlias("orderAmount","交易金额");
        writer.setColumnWidth(7, 10);
        writer.addHeaderAlias("fixedAmount","支付金额");
        writer.setColumnWidth(8, 10);
        writer.addHeaderAlias("firstMerchantFee","1级码商佣金");
        writer.setColumnWidth(9, 10);
        writer.addHeaderAlias("orderStatusStr","订单状态");
        writer.setColumnWidth(10, 10);
        writer.addHeaderAlias("callbackStatusStr","回调状态");
        writer.setColumnWidth(11, 10);
        writer.addHeaderAlias("accountInfo","收款信息");
        writer.setColumnWidth(12, 50);
        writer.addHeaderAlias("orderTime","创建时间");
        writer.setColumnWidth(13, 20);
        writer.addHeaderAlias("finishTime","更新时间");
        writer.setColumnWidth(14, 20);
        writer.addHeaderAlias("orderRemark","备注");
        writer.setColumnWidth(15, 60);
        writer.setOnlyAlias(true);
        writer.write(exportList, true);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName= URLEncoder.encode("订单明细" + DateUtil.format(new Date(),"yyyyMMddHHmmss"),"UTF-8");
        response.setHeader("Content-Disposition","attachment;filename="+fileName+".xlsx");
        ServletOutputStream out=response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        IoUtil.close(out);
    }
}
