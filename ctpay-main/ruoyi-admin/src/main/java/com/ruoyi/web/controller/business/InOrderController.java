package com.ruoyi.web.controller.business;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.enums.OrderStatus;
import com.ruoyi.common.enums.SysRoleEnum;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DESUtil;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.RedisLock;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.system.domain.business.*;
import com.ruoyi.system.domain.dto.*;
import com.ruoyi.system.domain.vo.MerchantOrderTotalVO;
import com.ruoyi.system.service.business.*;
import com.ruoyi.system.service.business.impl.ShopOrderService;
import com.ruoyi.system.service.business.impl.TestCreateService;
import com.ruoyi.system.service.impl.SnowflakeIdService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author admin
 * @since 2024-10-20
 */
@RestController
@Api("订单")
@RequestMapping("/inOrderEntity")
@Slf4j
public class InOrderController extends BaseController {

    @Resource
    private InOrderService inOrderService;
    @Resource
    private SnowflakeIdService snowflakeIdService;
    @Resource
    private ShopService shopService;
    @Resource
    private ShopOrderService shopOrderService;
    @Resource
    private InOrderDetailService detailService;
    @Resource
    private MerchantService merchantService;
    @Resource
    private MerchantChannelService merchantChannelService;
    @Resource
    private TestCreateService testCreateService;
    @Resource
    private RedisLock redisLockUtils;
    @Resource
    private RedisUtils redisUtils;

    @ApiOperation("获取订单列表")
    @PreAuthorize("@ss.hasPermi('system:order:list')")
    @PostMapping("/list")
    public R<Page<InOrderEntity>> list(@RequestBody OrderQueryDTO queryDTO) {
        Page<InOrderEntity> rowPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        //queryWrapper组装查询where条件
        LambdaQueryWrapper<InOrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotEmpty(queryDTO.getShopOrderNo()), InOrderEntity::getShopOrderNo, StrUtil.trim(queryDTO.getShopOrderNo()))
                .like(StrUtil.isNotEmpty(queryDTO.getTradeNo()), InOrderEntity::getTradeNo, StrUtil.trim(queryDTO.getTradeNo()))
                .eq(ObjectUtil.isNotEmpty(queryDTO.getChannelId()), InOrderEntity::getChannelId, queryDTO.getChannelId())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getAccountNumber()), InOrderEntity::getAccountNumber, queryDTO.getAccountNumber())
                .like(StrUtil.isNotEmpty(queryDTO.getChannelName()), InOrderEntity::getChannelName, queryDTO.getChannelName())
                .like(StrUtil.isNotEmpty(queryDTO.getAccountRemark()), InOrderEntity::getAccountRemark, queryDTO.getAccountRemark())
                .like(StrUtil.isNotEmpty(queryDTO.getPayer()), InOrderEntity::getPayer, queryDTO.getPayer())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getOrderStatus()), InOrderEntity::getOrderStatus, queryDTO.getOrderStatus())
                .eq(ObjectUtil.isNotEmpty(queryDTO.getCallbackStatus()), InOrderEntity::getCallbackStatus, queryDTO.getCallbackStatus())
                .ge(StrUtil.isNotEmpty(queryDTO.getStartTime()), InOrderEntity::getOrderTime, queryDTO.getStartTime())
                .le(StrUtil.isNotEmpty(queryDTO.getEndTime()), InOrderEntity::getOrderTime, queryDTO.getEndTime());
        queryWrapper.orderByDesc(InOrderEntity::getId);
        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        if (sysUser.getIdentity() == 5) {
            queryWrapper.eq(InOrderEntity::getMerchantId, sysUser.getUserId());
        } else if (sysUser.getIdentity() != 1) {
            return R.ok(new Page<>());
        }
        return R.ok(inOrderService.page(rowPage, queryWrapper));
    }

    @ApiOperation("获取订单统计")
    @PreAuthorize("@ss.hasPermi('system:order:list')")
    @PostMapping("/listTatal")
    public R<MerchantOrderTotalVO> listTatal(@RequestBody OrderQueryDTO queryDTO) {
        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        if (sysUser.getIdentity() == 5) {
            queryDTO.setMerchantId(sysUser.getUserId());
        } else if (sysUser.getIdentity() != 1) {
            return R.ok(new MerchantOrderTotalVO());
        }

        if (StrUtil.isNotEmpty(queryDTO.getShopOrderNo())) {
            queryDTO.setShopOrderNo(StrUtil.trim(queryDTO.getShopOrderNo()));
        }
        if (StrUtil.isNotEmpty(queryDTO.getTradeNo())) {
            queryDTO.setTradeNo(StrUtil.trim(queryDTO.getTradeNo()));
        }
        return R.ok(inOrderService.merchantOrderTotal(queryDTO));
    }

    @ApiOperation("码商确认订单")
    @Log(title = "码商确认订单", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('system:order:repair')")
    @GetMapping("/repair")
    public AjaxResult repair(@RequestParam("id") Long id, @RequestParam(value = "safeCode", required = false) String safeCode, @RequestParam(value = "type", required = true) Integer type) {
        InOrderEntity order = inOrderService.getById(id);
        if (order == null) {
            return AjaxResult.error("订单已被删除");
        }
        if (!order.getMerchantId().equals(SecurityUtils.getUserId())) {
            return AjaxResult.error("您暂无权限操作该订单");
        }
        //订单副表
        InOrderDetailEntity detailEntity = detailService.getById(order.getId());
        //获取码商
        MerchantEntity merchant = merchantService.getById(order.getMerchantId());
        MerchantChannelEntity merchantChannel = merchantChannelService.getById(detailEntity.getMerchantChannelId());
        if (merchantChannel.getNeedSafecode() == 0) {
            if (!SecurityUtils.matchesPassword(safeCode, merchant.getSafeCode())) {
                return AjaxResult.error("安全码不正确");
            }
        }
        //判断余额是否充足,订单已超时，且码商余额不足时，不允许回调
        if (order.getOpcoin() != 1) {
            String merchantBalance = redisUtils.get(RedisKeys.merchantBalance + merchant.getUserId());
            if (StrUtil.isEmpty(merchantBalance) || new BigDecimal(merchantBalance).compareTo(order.getOrderAmount()) < 0) {
                return AjaxResult.error("余额不足，请充值");
            }
        }
        return inOrderService.repair(order, detailEntity);
    }

    @ApiOperation("码商未收到付款")
    @Log(title = "码商未收到付款", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('system:order:unPaid')")
    @GetMapping("/unPaid")
    public AjaxResult unPaid(@RequestParam("id") Long id, @RequestParam(value = "safeCode", required = false) String safeCode) {
        InOrderEntity order = inOrderService.getById(id);
        if (order == null) {
            return AjaxResult.error("订单已被删除");
        }
        if (!order.getMerchantId().equals(SecurityUtils.getUserId())) {
            return AjaxResult.error("您暂无权限操作该订单");
        }
        //订单副表
        InOrderDetailEntity detailEntity = detailService.getById(order.getId());
        //获取码商
        MerchantEntity merchant = merchantService.getById(order.getMerchantId());
        MerchantChannelEntity merchantChannel = merchantChannelService.getById(detailEntity.getMerchantChannelId());
        if (merchantChannel.getNeedSafecode() == 0) {
            if (!SecurityUtils.matchesPassword(safeCode, merchant.getSafeCode())) {
                return AjaxResult.error("安全码不正确");
            }
        }
        return inOrderService.unPaid(order, detailEntity);
    }


    @ApiOperation("代理确认订单")
    @Log(title = "代理确认订单", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('system:order:repair')")
    @GetMapping("/agentRepair")
    public AjaxResult agentRepair(@RequestParam("id") Long id, @RequestParam(value = "code", required = false) String code) {
        try {
            Integer googleNumber = Integer.parseInt(code);
        }catch (NumberFormatException e) {
            return AjaxResult.error("验证码错误");
        }
        if (!SecurityUtils.validGoogle(code)){
            return AjaxResult.error("验证码错误");
        }
        InOrderEntity order = inOrderService.getById(id);
        if (order == null) {
            return AjaxResult.error("订单已被删除");
        }
        MerchantEntity merchant = merchantService.getById(order.getMerchantId());
        if (!SecurityUtils.getUserId().equals(merchant.getAgentId())) {
            return AjaxResult.error("您暂无权限操作该订单");
        }
        //订单副表
        InOrderDetailEntity detailEntity = detailService.getById(order.getId());
        if (StrUtil.equals("https://admin.qiezizfb.xyz/api/order/moniCallShop", detailEntity.getCallShopUrl())) {
            return AjaxResult.error("这是一笔测试订单");
        }
        //判断余额是否充足,订单已超时，且码商余额不足时，不允许回调
        if (order.getOpcoin() != 1) {
            String merchantBalance = redisUtils.get(RedisKeys.merchantBalance + merchant.getUserId());
            if (StrUtil.isEmpty(merchantBalance) || new BigDecimal(merchantBalance).compareTo(order.getOrderAmount()) < 0) {
                return AjaxResult.error("余额不足，请充值");
            }
        }
        return inOrderService.repair(order, detailEntity);
    }

    @ApiOperation("代理点击未收到付款")
    @Log(title = "代理点击未收到付款", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('system:order:unPaid')")
    @GetMapping("/agentUnPaid")
    public AjaxResult agentUnPaid(@RequestParam("id") Long id, @RequestParam(value = "code", required = false) String code) {
        if (!SecurityUtils.validGoogle(code)){
            return AjaxResult.error("验证码错误");
        }
        InOrderEntity order = inOrderService.getById(id);
        if (order == null) {
            return AjaxResult.error("订单已被删除");
        }
        MerchantEntity merchant = merchantService.getById(order.getMerchantId());
        if (!SecurityUtils.getUserId().equals(merchant.getAgentId())) {
            return AjaxResult.error("您暂无权限操作该订单");
        }
        //订单副表
        InOrderDetailEntity detailEntity = detailService.getById(order.getId());
        return inOrderService.unPaid(order, detailEntity);
    }

    @ApiOperation("处理超时未提交订单")
    @Log(title = "处理超时未提交订单", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('system:order:backOrder')")
    @GetMapping("/backOrder")
    public AjaxResult backOrder(@RequestParam("id") Long id, @RequestParam(value = "code", required = false) String code) {
        try {
            Integer googleNumber = Integer.parseInt(code);
        }catch (NumberFormatException e) {
            return AjaxResult.error("验证码错误");
        }
        if (!SecurityUtils.validGoogle(code)){
            return AjaxResult.error("验证码错误");
        }
        InOrderEntity order = inOrderService.getById(id);
        if (order == null) {
            return AjaxResult.error("订单已被删除");
        }
        if (!SecurityUtils.hasRole(SysRoleEnum.ADMIN.getRoleKey())) {
            return AjaxResult.error("非法请求");
        }

        String lockOrderId = RedisKeys.CPAY_ORDER_OP_LOCK+id;
        if(redisLockUtils.getLock(lockOrderId,"1")){
            try {
                if(ObjectUtil.isNotNull(order) && (order.getOrderStatus() == OrderStatus.WAIT.getValue() || order.getOrderStatus() == OrderStatus.BACKING.getValue())) {
                    InOrderDetailEntity detail = detailService.getById(order.getId());
                    //**待支付的订单才会进入**//*
                    //**扣减之后才会归还**//*
                    if (order.getOpcoin() == 1) {
                        //返还码商预付金额
                        BigDecimal changeAmount = order.getOrderAmount();
                        merchantService.updateAmount(AmountChangeDTO.builder()
                                .userId(order.getMerchantId())
                                .userName(order.getMerchantName())
                                .changeAmount(changeAmount)
                                .amountType(1)
                                .changeType(1)
                                .remarks("订单" + order.getTradeNo() + "支付超时，返还余额")
                                .orderNo(order.getTradeNo())
                                .agentId(detail.getAgentId())
                                .build());
                    }
                    //**设置订单超时*/
                    order.setOpcoin(0);
                    order.setFinishTime(new Date());
                    order.setOrderStatus(OrderStatus.TIMEOUT.getValue());
                    inOrderService.updateById(order);
                    //释放码
                    redisUtils.delete(RedisKeys.lockedQrcode + order.getQrcodeId());
                    //每日金额和笔数减少
                    inOrderService.qrcodeLimitAmountAndCount(order.getQrcodeId(), order.getOrderAmount(), 1);
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                redisLockUtils.releaseLock(lockOrderId,"1");
            }
        }
        return AjaxResult.success();
    }


    @ApiOperation("管理员冲正")
    @Log(title = "管理员冲正", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('system:order:czorder')")
    @GetMapping("/czOrder")
    public AjaxResult czOrder(@RequestParam("id") Long id, @RequestParam(value = "code", required = false) String code) {
        if (!SecurityUtils.validGoogle(code)){
            return AjaxResult.error("验证码错误");
        }
        if (!SecurityUtils.hasRole(SysRoleEnum.AGENT.getRoleKey())) {
            return AjaxResult.error("您暂无权限操作该订单");
        }
        return inOrderService.czOrder(id);
    }

    @ApiOperation("后台创建测试单")
    @PreAuthorize("@ss.hasPermi('system:order:createTest')")
    @PostMapping("/createTest")
    public AjaxResult createTest(@RequestBody TestOrderReq req, HttpServletRequest request) {
//        List<MerchantEntity> merchantEntities = JSONUtil.toList(redisUtils.get(RedisKeys.merchantInfo), MerchantEntity.class);
//        merchantEntities.forEach(data -> {
//            if (!redisUtils.hasKey(RedisKeys.merchantBalance + data.getUserId())){
//                redisUtils.set(RedisKeys.merchantBalance + data.getUserId(), data.getBalance().toString());
//            }
//            if (!redisUtils.hasKey(RedisKeys.merchantCommission + data.getUserId())){
//                redisUtils.set(RedisKeys.merchantCommission + data.getUserId(), data.getBalance().toString());
//            }
//        });
        String ip = IpUtils.getIpAddr(request);
        if (SecurityUtils.getLoginUser().getUser().getIdentity() != 1 && SecurityUtils.getLoginUser().getUser().getIdentity() != 3) {
            return AjaxResult.error("您暂无权限创建测试单");
        }
        String orderNo = "S" + snowflakeIdService.nextId();
        ShopOrderReq orderReq = BeanUtil.copyProperties(req, ShopOrderReq.class);
        orderReq.setNotify_url(orderReq.getNotify_url() + "/order/moniCallShop");
        orderReq.setOut_trade_no(orderNo);
        orderReq.setReturn_url("http://www.baidu.com");
        orderReq.setBody("123");
        orderReq.setTime_stamp(System.currentTimeMillis() + "");
        ShopEntity shop = shopService.getById(req.getMchid());

        Map<String, Object> mapreq = BeanUtil.beanToMap(orderReq);
        mapreq.remove("sign");
        String signStr = DESUtil.getSingByMap(mapreq) + "&key=" + shop.getSignSecret();
        String signNew = DigestUtil.md5Hex(signStr);
        orderReq.setSign(signNew.toLowerCase());
        try {
            return shopOrderService.createOrderNew(orderReq,ip);
        } catch (ServiceException e) {
            e.printStackTrace();
            log.error("商户提交订单失败：{}", e.getMessage());
            return AjaxResult.error(1, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("商户提交订单异常：{}", e.getMessage());
        }
        return AjaxResult.error(1, "未知异常，请联系管理员");
    }

    @ApiOperation("后台创建测试单指定码商")
    @PreAuthorize("@ss.hasPermi('system:order:createTest')")
    @PostMapping("/createTestByMerchant")
    public AjaxResult createTestByMerchant(@RequestBody TestOrderByMerchantReq req, HttpServletRequest request) {
        String ip = IpUtils.getIpAddr(request);
        if (SecurityUtils.getLoginUser().getUser().getIdentity() != 1 && SecurityUtils.getLoginUser().getUser().getIdentity() != 3) {
            return AjaxResult.error("您暂无权限创建测试单");
        }
//        List<MerchantEntity> list = JSONUtil.toList(redisUtils.get(RedisKeys.merchantInfo), MerchantEntity.class);
//        list.forEach(data -> {
//            redisUtils.set(RedisKeys.merchantBalance + data.getUserId(), data.getBalance());
//        });
        String orderNo = "S" + snowflakeIdService.nextId();
        ShopOrderTestReq orderReq = BeanUtil.copyProperties(req, ShopOrderTestReq.class);
        orderReq.setNotify_url(orderReq.getNotify_url() + "/order/moniCallShop");
        orderReq.setOut_trade_no(orderNo);
        orderReq.setReturn_url("http://www.baidu.com");
        orderReq.setBody("123");
        orderReq.setTime_stamp(System.currentTimeMillis() + "");
        orderReq.setMerchantId(req.getMerchantId());
        ShopEntity shop = shopService.getById(req.getMchid());

        Map<String, Object> mapreq = BeanUtil.beanToMap(orderReq);
        mapreq.remove("sign");
        mapreq.remove("merchantId");
        String signStr = DESUtil.getSingByMap(mapreq) + "&key=" + shop.getSignSecret();
        String signNew = DigestUtil.md5Hex(signStr);
        orderReq.setSign(signNew.toLowerCase());
        try {
            return testCreateService.createOrder(orderReq,ip);
        } catch (ServiceException e) {
            e.printStackTrace();
            log.error("商户提交订单失败：{}", e.getMessage());
            return AjaxResult.error(1, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("商户提交订单异常：{}", e.getMessage());
        }
        return AjaxResult.error(1, "未知异常，请联系管理员");
    }
}
