package com.ruoyi.web.controller.business;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.enums.OrderStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DESUtil;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.system.domain.business.*;
import com.ruoyi.system.domain.dto.ShopOrderReq;
import com.ruoyi.system.domain.vo.SuitableQrcodeVO;
import com.ruoyi.system.domain.vo.SytQueryVO;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.business.InOrderDetailService;
import com.ruoyi.system.service.business.InOrderService;
import com.ruoyi.system.service.business.ReqOrderService;
import com.ruoyi.system.service.business.impl.ShopOrderService;
import com.ruoyi.system.telegram.SFTelegramBot;
import com.ruoyi.web.core.config.S3UploadAndGetUrl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Api("接单控制类")
@RequestMapping("/order")
@Slf4j(topic = "ct-business")
public class ThreeOrderController {

    @Resource
    private ShopOrderService shopOrderService;

    @Resource
    private InOrderService inOrderService;
    @Resource
    private InOrderDetailService inOrderDetailService;
    @Autowired
    private RedisUtils redisUtils;
    @Resource
    private ReqOrderService reqOrderService;
    @Resource
    private S3UploadAndGetUrl s3UploadAndGetUrl;
    @Resource
    private SFTelegramBot kmsfTelegramBot;
    @Resource
    private ISysUserService userService;


    @PostMapping("/create")
    @ApiOperation("商户提交订单")
    public AjaxResult create(ShopOrderReq req, HttpServletRequest request) {
        try {
            reqOrderService.addReqOrder(req);
            String ip = IpUtils.getIpAddr(request);
            if(redisUtils.hasKey(RedisKeys.clientIpList + ip)){
                return AjaxResult.error("您已被拉入黑名单，请联系管理员");
            }
            return shopOrderService.createOrder(req, ip);
        }catch (ServiceException e) {
            log.error("商户提交订单失败：{},请求参数：{}", e.getMessage(), JSONUtil.toJsonStr(req));
            try{
                //获取商户
                ShopEntity shop = new ShopEntity();
                List<ShopEntity> shopList = JSONUtil.toList(redisUtils.get(RedisKeys.shopInfo), ShopEntity.class);
                if (shopList != null && !shopList.isEmpty()) {
                    shop = shopList.stream().filter(a -> a.getUserId().toString().equals(req.getMchid())).findFirst().orElse(null);
                } else {
                    shop = new ShopEntity();
                }
                ChannelEntity channel = new ChannelEntity();
                List<ChannelEntity> channelList = JSONUtil.toList(redisUtils.get(RedisKeys.channelInfo), ChannelEntity.class);
                if (channelList != null && !channelList.isEmpty()) {
                    channel = channelList.stream().filter(a -> a.getChannelCode().equals(req.getChannel())).findFirst().orElse(null);
                } else {
                    channel = new ChannelEntity();
                }
                InOrderEntity order = InOrderEntity.builder()
                        .shopOrderNo(req.getOut_trade_no())
                        .orderAmount(new BigDecimal(req.getAmount()))
                        .channelId(channel.getId())
                        .channelName(channel.getChannelName())
                        .orderTime(new Date())
                        .finishTime(new Date())
                        .orderStatus(OrderStatus.CLOSED.getValue())
                        .build();
                InOrderDetailEntity detailEntity = InOrderDetailEntity.builder()
                        .shopId(Long.parseLong(req.getMchid()))
                        .shopName(shop.getShopName())
                        .agentId(shop.getAgentId())
                        .channelCode(req.getChannel())
                        .channelCode(req.getChannel())
                        .callShopUrl(req.getNotify_url())
                        .orderRemark(e.getMessage())
                        .build();
                inOrderService.save(order);
                detailEntity.setOrderId(order.getId());
                inOrderDetailService.save(detailEntity);

                //码子不足发送tg预警
               /* try{
                    SysUser sysUser = userService.selectUserById(1L);
                    String botList = sysUser.getBotList();
                    if (botList != null && !botList.isEmpty() && !redisUtils.hasKey(RedisKeys.merchantChannelLess + channel.getId())) {
                        String[] bots = botList.split(";");
                        for (String bot : bots) {
                            kmsfTelegramBot.sendReply(Long.parseLong(bot), "@qzzf7 @qzzf77 【" + channel.getChannelName() + "】码子不足，请及时补充");
                        }
                        redisUtils.set(RedisKeys.merchantChannelLess + channel.getId(), "1", 60 * 30);
                    }
                }catch (Exception e2) {

                }*/

            }catch (Exception e1) {
                log.error("存失败订单失败：{}", e1.getMessage());
            }
            return AjaxResult.error(1, "没有可用的二维码");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("商户提交订单异常：{},请求参数：{}", e.getMessage(),JSONUtil.toJsonStr(req));
        }
        return AjaxResult.error(1, "未知异常");
    }

    @PostMapping("/query")
    @ApiOperation("商户提交订单")
    public AjaxResult query(@RequestParam Map<String,Object> req){
        log.info("商户查询订单请求参数：{}", JSONUtil.toJsonStr(req));
        return shopOrderService.query(req);
    }

    @GetMapping("getOrderData")
    @ApiOperation("收银台查找信息")
    public R<SytQueryVO> getOrderData(@RequestParam("tradeNo")String tradeNo){
        try {
            return inOrderService.getSytByTradeNo(tradeNo);
        } catch (Exception e) {
            return R.fail();
        }
    }

    @GetMapping("setOrderPayer")
    @ApiOperation("设置订单付款人")
    public AjaxResult setOrderPayer(@RequestParam("tradeNo")String tradeNo, @RequestParam("payer")String payer){
        return inOrderService.setOrderPayer(tradeNo, payer);
    }

    /**
     * 通用上传请求（单个）
     */
    @PostMapping("/upload")
    public AjaxResult uploadFile(MultipartFile file) throws Exception
    {
        if (ObjectUtil.isNull(file)) {
            return AjaxResult.error("文件不允许为空");
        }
        try
        {
            // 上传并返回新文件名称
            String url = s3UploadAndGetUrl.uploadObjectAndGetUrl( IdUtils.simpleUUID() + "." + FileNameUtil.getSuffix(file.getOriginalFilename()),file).toString();
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", file.getName());
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }

    @GetMapping("uploadReceiptImg")
    @ApiOperation("回执单")
    public AjaxResult uploadReceiptImg(@RequestParam("tradeNo")String tradeNo, @RequestParam("receiptImg")String receiptImg){
        InOrderEntity order = inOrderService.getOne(Wrappers.lambdaQuery(InOrderEntity.class).eq(
                InOrderEntity::getTradeNo, tradeNo
        ));
        log.info("收银台用户信息：{},对应订单{}", tradeNo, JSONUtil.toJsonStr(order));
        if (ObjectUtil.isEmpty(order) || (order.getOrderStatus() != OrderStatus.WAIT.getValue() && order.getOrderStatus() != OrderStatus.BACKING.getValue())) {
            log.error("收银台请求信息错误：{}", tradeNo);
            return AjaxResult.error("订单已超时，请重新发起订单");
        }
        if (ObjectUtil.isNotEmpty(order)) {
            if (StrUtil.isNotEmpty(order.getReceiptImg())) {
                return AjaxResult.error("您已上传过回单");
            }
            order.setReceiptImg(receiptImg);
            InOrderEntity saveOrder = new InOrderEntity();
            saveOrder.setId(order.getId());
            saveOrder.setReceiptImg(receiptImg);
            inOrderService.updateById(saveOrder);
            shopOrderService.sendReceiptImgToTg(order);
            return AjaxResult.success();
        }
        return AjaxResult.error("订单不存在");
    }


    @PostMapping("/moniCallShop")
    @ApiOperation("商户提交订单")
    public String moniCallShop(ShopOrderReq map){
        log.info("模拟回调参数：{}", JSONUtil.toJsonStr(map));
        return "SUCCESS";
    }

    private SuitableQrcodeVO getNext(List<SuitableQrcodeVO> list){
        List<SuitableQrcodeVO> result = new ArrayList<>();
        String preKey = RedisKeys.orderPreMerchantChannel + "10240" + ":" + 1106;
        try {
            list = list.stream().sorted(Comparator.comparing(SuitableQrcodeVO::getMerchantId)).collect(Collectors.toList());
            if (redisUtils.hasKey(preKey)) {
                long preMerchantId = redisUtils.get(preKey, Long.class);
                List<SuitableQrcodeVO> newList = list.stream().filter(a -> a.getMerchantId() > preMerchantId).collect(Collectors.toList());
                if (!newList.isEmpty()) {
                    result.add(newList.get(0));
                } else {
                    result.add(list.get(0));
                }
            } else {
                result.add(list.get(0));
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            redisUtils.set(preKey, result.get(0).getMerchantId());
        }
        return result.get(0);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1; i++) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("mchid", "10057");
            jsonObject.set("out_trade_no", "abc" + RandomUtil.randomNumbers(8));
            jsonObject.set("amount", "100");
            jsonObject.set("channel", "alipayCodeSmall");
            jsonObject.set("notify_url", "https://admin.fxfdc.xyz/api/order/moniCallShop");
            jsonObject.set("return_url", "https://www.baidu.com");
            jsonObject.set("time_stamp", System.currentTimeMillis());
            jsonObject.set("body", "张三" + RandomUtil.randomNumbers(2));
            String signStr = DESUtil.getSingByMap(jsonObject) + "&key=" + "fd07e676e508a8d94cb6d5b69bbc5d01fd07e676e508a8d9";
            String signNew = DigestUtil.md5Hex(signStr);
            jsonObject.set("sign", signNew);
            //提交订单
            HttpRequest request = HttpUtil.createPost("https://admin.fxfdc.xyz/api/order/create");
            //连接超时5秒钟
            request.setConnectionTimeout(5000);
            request.form(jsonObject);
            HttpResponse httpResponse = request.execute();
            String result = httpResponse.body();
            log.info("提交第三方平台订单返回参数：{}", JSONUtil.toJsonStr(JSONUtil.parseObj(result)));
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
