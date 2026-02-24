package com.ruoyi.system.telegram;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.business.ChannelEntity;
import com.ruoyi.system.domain.business.InOrderEntity;
import com.ruoyi.system.domain.business.ShopEntity;
import com.ruoyi.system.domain.vo.OrderReportVO;
import com.ruoyi.system.service.business.InOrderService;
import com.ruoyi.system.service.business.ShopBaseChannelService;
import com.ruoyi.system.service.business.ShopService;
import com.ruoyi.system.telegram.tgDataEntity.TgOrderInfo;
import com.ruoyi.system.telegram.tgutil.TgUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.payments.OrderInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class SFTelegramBot extends TelegramLongPollingBot {

    private final TelegramBotConfig config;

    // 用于存储每个群组的最后两条消息记录
    private Map<Long, Message> lastMessages = new HashMap<>();

    @Value("${telegram.switch}")
    private String botSwitch;

    @Autowired
    private InOrderService inOrderService;
    public SFTelegramBot(TelegramBotConfig config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getUsername();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message newMessage = update.getMessage();
        long chatId = newMessage.getChatId();
        long fromUserId = newMessage.getFrom().getId();
        // 更新该群组的最后两条消息记录
        lastMessages.put(chatId, newMessage);
        // 处理接收到的消息(公共消息)
        if (StringUtils.isNotEmpty(update.getMessage().getText())&&isMathExpression(update.getMessage().getText())&&containsOperator(update.getMessage().getText())) {
            DoubleEvaluator evaluator = new DoubleEvaluator();
            double result = evaluator.evaluate(update.getMessage().getText());
            sendReply(chatId,result+"");
        }else if("gid".equals(update.getMessage().getText())){
            sendReply(chatId,chatId+"");
        }else if("uid".equals(update.getMessage().getText())){
            sendReply(chatId,fromUserId+"");
        }
        String chatKey = chatId+"";
        if(TgUserUtil.tgBusinessUserMap.containsKey(chatKey)){
            // 处理商户发送的消息
            if (!isValuePresentInMap(TgUserUtil.tgBusinessUserMap, fromUserId+"")){
                //处理商户消息
                //是为引用的消息
                Message lastMessage = lastMessages.get(chatId);
                if(lastMessage.isReply()){
                    String shKey = chatId+""+lastMessage.getReplyToMessage().getMessageId();
                    if(!TgUserUtil.messageMap.containsKey(shKey)){
                        return;
                    }
                    List<TgOrderInfo> ptList = TgUserUtil.messageMap.get(shKey);
                    for(TgOrderInfo orderInfo:ptList){
                        if(lastMessage.hasText()){
                            sendReplyByMessageId(Long.valueOf(orderInfo.getPtChatId()),getNoOrderNumMessage(lastMessage.getText()),orderInfo.getPtMessageId());
                        }else if(lastMessage.hasPhoto()){
                            if(org.apache.commons.lang3.StringUtils.isNotEmpty(lastMessage.getCaption())){
                                sendPhotoReplyByMessageId(orderInfo.getPtChatId(), lastMessage.getPhoto().get(0).getFileId(), getNoOrderNumMessage(lastMessage.getCaption()),Integer.valueOf(orderInfo.getPtMessageId()));
                            }else {
                                sendPhotoReplyByMessageId(orderInfo.getPtChatId(), lastMessage.getPhoto().get(0).getFileId(), "",Integer.valueOf(orderInfo.getPtMessageId()));
                            }
                        }else if(lastMessage.hasVideo()){
                            if(org.apache.commons.lang3.StringUtils.isNotEmpty(lastMessage.getCaption())){
                                sendVideoReplyByMessageId(orderInfo.getPtChatId(), lastMessage.getVideo().getFileId(), getNoOrderNumMessage(lastMessage.getCaption()),Integer.valueOf(orderInfo.getPtMessageId()));
                            }else {
                                sendVideoReplyByMessageId(orderInfo.getPtChatId(), lastMessage.getVideo().getFileId(), "",Integer.valueOf(orderInfo.getPtMessageId()));
                            }
                        }
                    }
                }else{
                    //不是引用的消息
                    //图文消息
                    if(lastMessage.hasPhoto()&& org.apache.commons.lang3.StringUtils.isNotEmpty(lastMessage.getCaption())){
                        List<String> list = getOrderNum(lastMessage.getCaption());
                        if(list!=null&&list.size()>=0){
                            Map<String, TgOrderInfo> shResultMap = queryOrderInfoBySh(list.get(0));
                            String shKey = chatId+""+lastMessage.getMessageId();
                            List<TgOrderInfo> shList = new ArrayList<>();
                            for (Map.Entry<String, TgOrderInfo> entry : shResultMap.entrySet()) {
                                TgOrderInfo orderInfo = entry.getValue();
                                if(org.apache.commons.lang3.StringUtils.isNotEmpty(orderInfo.getPtChatId())&& org.apache.commons.lang3.StringUtils.isNotEmpty(orderInfo.getOrderNo())) {
                                    int ptmessageId =  sendPhotoReply(orderInfo.getPtChatId(), lastMessage.getPhoto().get(0).getFileId(), orderInfo.getOrderNo()+" "+getChinese(lastMessage.getCaption()));
                                    //设置商户对应的平台消息
                                    if(ptmessageId>0){
                                        sendReply(chatId,"订单处理中........");
                                        orderInfo.setShChatId(chatId+"");
                                        orderInfo.setPtMessageId(ptmessageId);
                                        orderInfo.setShMessageId(lastMessage.getMessageId());
                                        orderInfo.setMessageDate(new Date());
                                        shList.add(orderInfo);
                                        //设置平台消息
                                        List<TgOrderInfo> ptList = new ArrayList<>();
                                        ptList.add(orderInfo);
                                        TgUserUtil.messageMap.put(orderInfo.getPtChatId()+ptmessageId,ptList);
                                    }
                                }
                            }
                            //设置商户消息
                            TgUserUtil.messageMap.put(shKey,shList);
                        }
                    }
                }
            }else{
                //处理非商户消息
            }
        }else if(TgUserUtil.tgPlateFormUserMap.containsKey(chatKey)){
            // 处理平台发送的消息
            Message lastMessage = lastMessages.get(chatId);
            //是为引用的消息
            if(lastMessage.isReply()){
                if(lastMessage.getText()!=null) {
                    if (lastMessage.getText().trim().startsWith("+") || lastMessage.getText().trim().startsWith("-") || lastMessage.getText().trim().startsWith("/")) {
                        return;
                    }
                }
                String ptKey = lastMessage.getChatId()+""+lastMessage.getReplyToMessage().getMessageId();
                if(TgUserUtil.messageMap.containsKey(ptKey)){
                    List<TgOrderInfo> fromMessageList = TgUserUtil.messageMap.get(ptKey);
                    if(fromMessageList!=null&&fromMessageList.size()>0){
                        TgOrderInfo fromMessage = fromMessageList.get(0);
                        if(lastMessage.hasText()){
                            sendReplyByMessageId(Long.valueOf(fromMessage.getShChatId()),getNoOrderNumMessage(lastMessage.getText()),Integer.valueOf(fromMessage.getShMessageId()));
                        }else if(lastMessage.hasPhoto()){
                            if(org.apache.commons.lang3.StringUtils.isNotEmpty(lastMessage.getCaption())) {
                                sendPhotoReplyByMessageId(fromMessage.getShChatId(), lastMessage.getPhoto().get(0).getFileId(), getNoOrderNumMessage(lastMessage.getCaption()),Integer.valueOf(fromMessage.getShMessageId()));
                            }else {
                                sendPhotoReplyByMessageId(fromMessage.getShChatId(), lastMessage.getPhoto().get(0).getFileId(), "",Integer.valueOf(fromMessage.getShMessageId()));
                            }
                        }else if(lastMessage.hasVideo()){
                            if(org.apache.commons.lang3.StringUtils.isNotEmpty(lastMessage.getCaption())) {
                                sendVideoReplyByMessageId(fromMessage.getShChatId(), lastMessage.getVideo().getFileId(), getNoOrderNumMessage(lastMessage.getCaption()),Integer.valueOf(fromMessage.getShMessageId()));
                            }else {
                                sendVideoReplyByMessageId(fromMessage.getShChatId(), lastMessage.getVideo().getFileId(), "",Integer.valueOf(fromMessage.getShMessageId()));
                            }
                        }
                    }
                }
            }else{

            }
        }
    }
    public Map<String,TgOrderInfo> queryOrderInfoBySh(String orderNum) {
        TgOrderInfo queryEntity = new TgOrderInfo();
        queryEntity.setClientOrderNo(orderNum);
        TgOrderInfo orderInfo = inOrderService.queryTgOrder(queryEntity);
        Map<String,TgOrderInfo> resultMap = new HashMap<>();
        resultMap.put(orderInfo.getOrderNo(),orderInfo);
        return  resultMap;
    }

    //是否存在map中
    public boolean isValuePresentInMap(Map<String, String> map, String target) {
        for (String value : map.values()) {
            // 检查目标字符串是否存在于逗号分隔的值中
            List<String> valuesList = Arrays.asList(value.split(","));
            if (valuesList.contains(target)) {
                return true;
            }
        }
        return false;
    }
    public  String getChinese(String input) {
        // 正则表达式，匹配中文字符
        Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]+");
        Matcher matcher = pattern.matcher(input);
        // 查找匹配的中文字符串
        String result = "";
        while (matcher.find()) {
            String chinese = matcher.group();
            if("飞天数字".equals(chinese.trim())||"创建时间".equals(chinese.trim())||"飞天支付宝".equals(chinese.trim())){
            }else {
                result = result + chinese;
            }
        }
        return result;
    }



    @PostConstruct
    public void start() {
        if ("on".equalsIgnoreCase(botSwitch)) {
            try {
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                botsApi.registerBot(this);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            System.out.println("查单机器人已启动.");
        } else {
            System.out.println("查单机器人已关闭.");
        }
    }

    private boolean isMathExpression(String messageText) {
        // 正则表达式匹配一个简单的数学表达式 (包含数字和 +, -, *, /, (), 空格)
        return messageText.matches("[0-9\\.\\s\\+\\-\\*/\\(\\)]+");
    }
    // 检查是否包含数学操作符
    private  boolean containsOperator(String input) {
        return input.contains("+") || input.contains("-") ||
                input.contains("*") || input.contains("/") ||
                input.contains("(") || input.contains(")");
    }
    //获取消息中的单号
    public List<String> getOrderNum(String input){
        List<String> list = new ArrayList<>();
        // 定义匹配订单号和商户号的正则表达式模式
        String pattern = "\\b[A-Za-z0-9]{12,}|[A-Za-z0-9-]{36}\\b";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        // 创建 Matcher 对象
        Matcher m = r.matcher(input);
        // 打印匹配的订单号和商户号
        while (m.find()) {
            list.add(m.group(0));
        }
        return list;
    }

    public static Double calculate(String expression) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        Object result = engine.eval(expression);

        if (result instanceof Number) {
            return ((Number) result).doubleValue();
        } else {
            throw new IllegalArgumentException("表达式结果不是数字类型");
        }
    }

    public int sendPhotoReply(String chatId, String photoId,String text) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(photoId));
        sendPhoto.setCaption(text);
        try {
            int id = execute(sendPhoto).getMessageId(); // 发送图片消息
            return id;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public int sendPhotoReplyByMessageId(String chatId, String photoId,String text,int messageId) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(photoId));
        sendPhoto.setCaption(text);
        sendPhoto.setReplyToMessageId(messageId);
        try {
            int id = execute(sendPhoto).getMessageId(); // 发送图片消息
            return id;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public int sendPhotoByReceiptImg(String chatId, String photoPath,String text) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        try {
            URL url = new URL(photoPath);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            String fileName = System.currentTimeMillis() + FileUtil.getName(photoPath);
            InputFile inputFile = new InputFile(inputStream, fileName);
            sendPhoto.setPhoto(inputFile);
            sendPhoto.setCaption(text);
            // 发送图片消息
            return execute(sendPhoto).getMessageId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int sendVideoReplyByMessageId(String chatId, String videoId,String text,int messageId) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chatId);
        sendVideo.setVideo(new InputFile(videoId));
        sendVideo.setCaption(text);
        sendVideo.setReplyToMessageId(messageId);
        try {
            int id = execute(sendVideo).getMessageId(); // 发送视频消息
            return id;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public void sendReply(long chatId, String text) {
        try {
            execute(
                    SendMessage.builder()
                            .chatId(String.valueOf(chatId))
                            .text(text)
                            .build()
            );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public void sendReplyByMessageId(long chatId, String text,int messageId) {
        try {
            execute(
                    SendMessage.builder()
                            .chatId(String.valueOf(chatId))
                            .replyToMessageId(messageId)
                            .text(text)
                            .build()
            );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getNoOrderNumMessage(String input){
        String tempString = input;
        // 定义订单号的正则表达式模式
        String pattern = "\\b[A-Za-z0-9]{12,}\\b";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        // 创建 Matcher 对象
        Matcher m = r.matcher(input);
        String orderNum = "";
        // 打印匹配的订单号
        while (m.find()) {
            orderNum =  m.group(0);
            tempString = tempString.replaceAll(orderNum,"");
        }
        return tempString;
    }
}
