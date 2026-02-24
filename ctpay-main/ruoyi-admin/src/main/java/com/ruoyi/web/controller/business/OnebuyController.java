package com.ruoyi.web.controller.business;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.utils.DESUtil;
import com.ruoyi.common.utils.TripleDESUtil;
import com.ruoyi.system.domain.dto.OnebuyOrderReq;
import com.ruoyi.system.domain.dto.OnebuyReq;
import com.ruoyi.system.service.business.OnebuyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Api("一码通控制类")
@RequestMapping("/onebuyApi")
@Slf4j(topic = "ct-business")
public class OnebuyController {

    public static final String DES_SECRET = "Tx&sf$3BxoQdlEbxTx8$#x0p";

    @Resource
    private OnebuyService onebuyService;

    @PostMapping("/shops")
    @ApiOperation("心跳")
    public String shops(@RequestBody OnebuyReq req) {
        try {

            String heartbeatData = TripleDESUtil.decrypt(req.getData(), DES_SECRET);
            log.info("一码通心跳参数：{}", heartbeatData);
            JSONArray shopIdArr = JSONUtil.parseArray(heartbeatData);
            onebuyService.heartbeat(shopIdArr.toList(String.class));
        } catch (Exception e) {
            log.error("一码通心跳失败：{},请求参数：{}", e.getMessage(),JSONUtil.toJsonStr(req), e);
        }
        return "success";
    }


    @PostMapping("/online")
    @ApiOperation("店铺上线")
    public String online(@RequestBody OnebuyReq req) {
        try {
            String onlineData = TripleDESUtil.decrypt(req.getData(), DES_SECRET);
            log.info("一码通店铺上线参数：{}", onlineData);
            JSONArray shopIdArr = JSONUtil.parseArray(onlineData);
            onebuyService.online(shopIdArr.toList(String.class));
        } catch (Exception e) {
            log.error("一码通店铺上线失败：{},请求参数：{}", e.getMessage(),JSONUtil.toJsonStr(req), e);
        }
        return "success";
    }


    @PostMapping("/offline")
    @ApiOperation("店铺下线")
    public String offline(@RequestBody OnebuyReq req) {
        try {
            String offlineData = TripleDESUtil.decrypt(req.getData(), DES_SECRET);
            JSONArray shopIdArr = JSONUtil.parseArray(offlineData);
            log.info("一码通店铺下线参数：{}", offlineData);
            onebuyService.offline(shopIdArr.toList(String.class));
        } catch (Exception e) {
            log.error("一码通店铺下线失败：{},请求参数：{}", e.getMessage(),JSONUtil.toJsonStr(req), e);
        }
        return "success";
    }


    @PostMapping("/orderReport")
    @ApiOperation("订单上报")
    public String orderReport(@RequestBody OnebuyReq req) {
        try {
            String orderData = TripleDESUtil.decrypt(req.getData(), DES_SECRET);
            log.info("一码通订单上报参数：{}", orderData);
            JSONArray shopIdArr = JSONUtil.parseArray(orderData);
            onebuyService.orderReport(shopIdArr.toList(OnebuyOrderReq.class));
        } catch (Exception e) {
            log.error("一码通店铺订单上报失败：{},请求参数：{}", e.getMessage(),JSONUtil.toJsonStr(req), e);
        }
        return "success";
    }

}
