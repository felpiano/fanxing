package com.ruoyi.web.controller.business;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.enums.SysRoleEnum;
import com.ruoyi.common.utils.DESUtil;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.business.InOrderDetailEntity;
import com.ruoyi.system.domain.business.InOrderEntity;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.ruoyi.system.domain.dto.MerchantQrcodeOrderDTO;
import com.ruoyi.system.domain.dto.ReportForAgentDTO;
import com.ruoyi.system.domain.dto.ShopOrderReq;
import com.ruoyi.system.domain.dto.TestOrderReq;
import com.ruoyi.system.domain.vo.*;
import com.ruoyi.system.service.business.InOrderDetailService;
import com.ruoyi.system.service.business.InOrderService;
import com.ruoyi.system.service.business.MerchantQrcodeService;
import com.ruoyi.system.service.business.MerchantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@Api("统计信息")
@RequestMapping("/report")
@Slf4j
public class ReportController {
    @Resource
    private InOrderDetailService detailService;
    @Resource
    private MerchantQrcodeService merchantQrcodeService;
    @Resource
    private InOrderService inOrderService;
    @Resource
    private MerchantService merchantService;

    @ApiOperation("码商首页统计")
    @PreAuthorize("@ss.hasPermi('system:eport:homeReport')")
    @PostMapping("/homeReport")
    public R<MerchantOrderReportVO> homeReport() {
        return R.ok(detailService.reportOrderByDate());
    }

    @ApiOperation("代理首页统计")
    @PreAuthorize("@ss.hasPermi('system:eport:agentHomeReport')")
    @GetMapping("/agentHomeReport")
    public R<MerchantOrderReportVO> agentHomeReport(@RequestParam("type") Integer type) {
        return R.ok(detailService.reportAgent(type));
    }

    @ApiOperation("代理查询通道管理")
    @PreAuthorize("@ss.hasPermi('system:eport:agentQrcodeOrder')")
    @PostMapping("/agentQrcodeOrder")
    public R<IPage<MerchantQrcodeOrderVO>> agentQrcodeOrder(@RequestBody MerchantQrcodeOrderDTO dto){
        if (SecurityUtils.hasRole(SysRoleEnum.ADMIN.getRoleKey())) {
            return R.ok(detailService.reportMerchantQrcodeOrders(dto));
        }else if (SecurityUtils.hasRole(SysRoleEnum.AGENT.getRoleKey())) {
            dto.setAgentId(SecurityUtils.getUserId());
            return R.ok(detailService.reportMerchantQrcodeOrders(dto));
        }
        return R.fail("非法请求");
    }

    @ApiOperation("代理查询通道管理统计")
    @PreAuthorize("@ss.hasPermi('system:eport:agentQrcodeOrder')")
    @PostMapping("/agentQrReport")
    public R<MerchantQrcodeReportVO> agentQrReport(@RequestBody MerchantQrcodeOrderDTO dto){
        if (SecurityUtils.hasRole(SysRoleEnum.ADMIN.getRoleKey())) {
            return R.ok(merchantQrcodeService.reportQrcode(dto));
        } else if (SecurityUtils.hasRole(SysRoleEnum.AGENT.getRoleKey())) {
            dto.setAgentId(SecurityUtils.getUserId());
            return R.ok(merchantQrcodeService.reportQrcode(dto));
        }
        return R.fail("非法请求");
    }

    @ApiOperation("代理对账报表")
    @PreAuthorize("@ss.hasPermi('system:eport:reportForAgent')")
    @PostMapping("/reportForAgent")
    public R<List<ReportForAgentVO>> reportForAgent(@RequestBody ReportForAgentDTO dto){
        if (StrUtil.isEmpty(dto.getStartTime())) {
            dto.setStartTime(DateUtil.formatDateTime(DateUtil.beginOfDay(new Date())));
        }
        if (StrUtil.isEmpty(dto.getEndTime())) {
            dto.setEndTime(DateUtil.formatDateTime(DateUtil.endOfDay(new Date())));
        }
        if (SecurityUtils.hasRole(SysRoleEnum.AGENT.getRoleKey())) {
            dto.setAgentId(SecurityUtils.getUserId());
            return R.ok(detailService.reportForAgent(dto));
        } else if(SecurityUtils.hasRole(SysRoleEnum.ADMIN.getRoleKey())) {
            return R.ok(detailService.reportForAgent(dto));
        }
        return R.fail("非法访问");
    }

    @ApiOperation("代理统计报表")
    @PreAuthorize("@ss.hasPermi('system:eport:reportByCondition')")
    @PostMapping("/reportByCondition")
    public R<List<ReportForAgentVO>> reportByCondition(@RequestBody ReportForAgentDTO dto){
        if (SecurityUtils.hasRole(SysRoleEnum.AGENT.getRoleKey())) {
            dto.setAgentId(SecurityUtils.getUserId());
            return R.ok(detailService.reportByCondition(dto));
        } else if(SecurityUtils.hasRole(SysRoleEnum.ADMIN.getRoleKey())) {
            return R.ok(detailService.reportByCondition(dto));
        } else if (SecurityUtils.hasRole(SysRoleEnum.MERCHANT.getRoleKey())) {
            dto.setMerchantId(SecurityUtils.getUserId());
            return R.ok(detailService.reportByCondition(dto));
        }
        return R.fail("非法访问");
    }


    @ApiOperation("代理统计报表日期选择")
    @PreAuthorize("@ss.hasPermi('system:eport:reportForAgent')")
    @PostMapping("/reportForAgentByDate")
    public R<List<ReportForAgentVO>> reportForAgentByDate(@RequestBody ReportForAgentDTO dto){
        if (SecurityUtils.hasRole(SysRoleEnum.AGENT.getRoleKey())) {
            dto.setAgentId(SecurityUtils.getUserId());
            return R.ok(detailService.reportForAgentByDate(dto));
        } else if(SecurityUtils.hasRole(SysRoleEnum.ADMIN.getRoleKey())) {
            return R.ok(detailService.reportForAgentByDate(dto));
        }
        return R.fail("非法访问");
    }

    @ApiOperation("每个码商的对账")
    @PreAuthorize("@ss.hasPermi('system:eport:reportForAgent')")
    @PostMapping("/allMerchantReport")
    public R<List<ReportForAgentVO>> allMerchantReport(@RequestBody ReportForAgentDTO dto){
        if (SecurityUtils.hasRole(SysRoleEnum.AGENT.getRoleKey())) {
            dto.setAgentId(SecurityUtils.getUserId());
            return R.ok(detailService.allMerchantReport(dto));
        } else if(SecurityUtils.hasRole(SysRoleEnum.ADMIN.getRoleKey())) {
            return R.ok(detailService.allMerchantReport(dto));
        }
        return R.fail("非法访问");
    }

    @ApiOperation("获取每个码商的对账报表")
    @PreAuthorize("@ss.hasPermi('system:order:list')")
    @PostMapping("/reportExport")
    public void reportExport(@RequestBody ReportForAgentDTO dto, HttpServletResponse response) throws IOException {
        if (SecurityUtils.hasRole(SysRoleEnum.AGENT.getRoleKey())) {
            dto.setAgentId(SecurityUtils.getUserId());
        }
        List<ReportForAgentVO> exportList = detailService.allMerchantReport(dto);
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.addHeaderAlias("userId","码商ID");
        writer.addHeaderAlias("userName","码商名称");
        writer.addHeaderAlias("yesBalance","昨日余额");
        writer.addHeaderAlias("todayBalance","今日余额");
        writer.addHeaderAlias("chargeMoney","今日上分");
        writer.addHeaderAlias("successMoney","成功金额");
        writer.addHeaderAlias("merchantFee","佣金");
        writer.addHeaderAlias("chargeToMoney","差额");
        writer.setOnlyAlias(true);
        writer.write(exportList, true);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName= URLEncoder.encode("报表信息" + DateUtil.format(new Date(),"yyyyMMddHHmmss"),"UTF-8");
        response.setHeader("Content-Disposition","attachment;filename="+fileName+".xlsx");
        ServletOutputStream out=response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        IoUtil.close(out);
    }

    @ApiOperation("码商对账报表")
    @PreAuthorize("@ss.hasPermi('system:eport:reportForMerchant')")
    @PostMapping("/reportForMerchant")
    public R<List<ReportForAgentVO>> reportForMerchant(@RequestBody ReportForAgentDTO dto){
        if (StrUtil.isEmpty(dto.getStartTime())) {
            dto.setStartTime(DateUtil.formatDateTime(DateUtil.beginOfDay(new Date())));
        }
        if (StrUtil.isEmpty(dto.getEndTime())) {
            dto.setEndTime(DateUtil.formatDateTime(DateUtil.endOfDay(new Date())));
        }
        return R.ok(detailService.reportForMerchant(dto));
    }

    @ApiOperation("佣金列表")
    @PreAuthorize("@ss.hasPermi('system:eport:commissionList')")
    @PostMapping("/commissionList")
    public R<List<CommissionVO>> commissionList(@RequestBody ReportForAgentDTO dto){
        if (StrUtil.isEmpty(dto.getStartTime())) {
            dto.setStartTime(DateUtil.formatDateTime(DateUtil.beginOfDay(new Date())));
        } else {
            dto.setStartTime(dto.getStartTime() + " 00:00:00");
        }
        if (StrUtil.isEmpty(dto.getEndTime())) {
            dto.setEndTime(DateUtil.formatDateTime(DateUtil.endOfDay(new Date())));
        } else {
            dto.setEndTime(dto.getEndTime() + " 23:59:59");
        }
        if (SecurityUtils.hasRole(SysRoleEnum.AGENT.getRoleKey())) {
            dto.setAgentId(SecurityUtils.getUserId());
            if (StrUtil.isNotEmpty(dto.getMerchantNameOne())) {
                MerchantEntity merchantOne = merchantService.getOne(Wrappers.lambdaQuery(MerchantEntity.class).eq(MerchantEntity::getMerchantName, dto.getMerchantNameOne()));
                if (merchantOne != null) {
                    dto.setChildPathStart(merchantOne.getParentPath());
                } else {
                    dto.setChildPathStart(dto.getMerchantNameOne());
                }
            } else if (ObjectUtil.isNotNull(dto.getMerchantIdOne())){
                MerchantEntity merchant = merchantService.getById(dto.getMerchantIdOne());
                if (merchant != null) {
                    dto.setChildPathStart(merchant.getParentPath());
                }
            }
        }else if (SecurityUtils.hasRole(SysRoleEnum.MERCHANT.getRoleKey())) {
            MerchantEntity merchant = merchantService.getById(SecurityUtils.getUserId());
            if (StrUtil.isNotEmpty(dto.getMerchantNameOne())) {
                MerchantEntity merchantOne = merchantService.getOne(Wrappers.lambdaQuery(MerchantEntity.class).
                        eq(MerchantEntity::getMerchantName, dto.getMerchantNameOne())
                        .likeRight(MerchantEntity::getParentPath, merchant.getParentPath()));
                if (merchantOne != null) {
                    dto.setChildPathStart(merchantOne.getParentPath());
                } else {
                    dto.setChildPathStart(dto.getMerchantNameOne());
                }
            } else {
                dto.setChildPathStart(merchant.getParentPath());
            }
            dto.setNeMerchantId(merchant.getUserId());
            dto.setMerchantLevel(merchant.getMerchantLevel());
        } else {
            return R.fail("非法请求");
        }
        return R.ok(detailService.commissionList(dto));
    }

    @ApiOperation("测试拉单")
//    @GetMapping("/test")
    public AjaxResult test(Integer nums){
        for (int i = 0; i < nums; i++) {
            Thread thread = new Thread(() -> {
                TestOrderReq req = new TestOrderReq();
                req.setMchid("10053");
                req.setAmount("10");
                req.setChannel("alipayCodeSmall");
                req.setNotify_url("https://admin.qiezizfb.xyz/api/order/moniCallShop");
                String orderNo = "S" + RandomUtil.randomNumbers(12);
                ShopOrderReq orderReq = BeanUtil.copyProperties(req, ShopOrderReq.class);
                orderReq.setNotify_url(orderReq.getNotify_url());
                orderReq.setOut_trade_no(orderNo);
                orderReq.setReturn_url("http://www.baidu.com");
                orderReq.setBody("123");
                orderReq.setTime_stamp(System.currentTimeMillis() + "");
                Map<String, Object> mapreq = BeanUtil.beanToMap(orderReq);
                mapreq.remove("sign");
                String signStr = DESUtil.getSingByMap(mapreq) + "&key=" + "9b4301f225544f4f2c29c8fe5b1a19e69b4301f225544f4f";
                String signNew = DigestUtil.md5Hex(signStr);
                orderReq.setSign(signNew.toLowerCase());
                Map<String,Object> reqs = BeanUtil.beanToMap(orderReq);
//                HttpRequest request = HttpUtil.createPost("https://admin.qiezizfb.xyz/api/order/create");
                HttpRequest request = HttpUtil.createPost("http://localhost:8090/order/create");
                //连接超时5秒钟
                request.setConnectionTimeout(5000);
                request.form(reqs);
                HttpResponse httpResponse = request.execute();
                String result = httpResponse.body();
                System.out.println("回调商户返回参数：" + result);

                JSONObject jsonObject = JSONUtil.parseObj(result);
                JSONObject data = jsonObject.getJSONObject("data");
                String tradeNo = data.getStr("tradeNo");
                InOrderEntity inOrderEntity = inOrderService.getOne(Wrappers.lambdaQuery(InOrderEntity.class)
                        .eq(InOrderEntity::getTradeNo, tradeNo));
                InOrderDetailEntity detailEntity = detailService.getById(inOrderEntity.getId());
                inOrderService.repair(inOrderEntity, detailEntity);
            });
            thread.start();
        }
        return AjaxResult.success();
    }

    public static void main(String[] args) {
        TestOrderReq req = new TestOrderReq();
        req.setMchid("10057");
        req.setAmount("10");
        req.setChannel("alipayCodeSmall");
        req.setNotify_url("https://admin.qiezizfb.xyz/api/order/moniCallShop");
        String orderNo = "S" + RandomUtil.randomNumbers(12);
        ShopOrderReq orderReq = BeanUtil.copyProperties(req, ShopOrderReq.class);
        orderReq.setNotify_url(orderReq.getNotify_url());
        orderReq.setOut_trade_no(orderNo);
        orderReq.setReturn_url("http://www.baidu.com");
        orderReq.setBody("123");
        orderReq.setTime_stamp(System.currentTimeMillis() + "");
        Map<String, Object> mapreq = BeanUtil.beanToMap(orderReq);
        mapreq.remove("sign");
        String signStr = DESUtil.getSingByMap(mapreq) + "&key=" + "fd07e676e508a8d94cb6d5b69bbc5d01fd07e676e508a8d9";
        String signNew = DigestUtil.md5Hex(signStr);
        orderReq.setSign(signNew.toLowerCase());
        Map<String,Object> reqs = BeanUtil.beanToMap(orderReq);
        System.out.println(JSONUtil.toJsonStr(reqs));
//        HttpRequest request = HttpUtil.createPost("http://localhost:8090/order/create");
        HttpRequest request = HttpUtil.createPost("https://admin.qiezizfb.xyz/api/order/create");
        //连接超时5秒钟
        request.setConnectionTimeout(5000);
        request.form(reqs);
        System.out.println();
        HttpResponse httpResponse = request.execute();
        String result = httpResponse.body();
        System.out.println("回调商户返回参数：" + result);
    }
}
