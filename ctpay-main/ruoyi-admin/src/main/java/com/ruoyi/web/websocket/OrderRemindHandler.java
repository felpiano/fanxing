package com.ruoyi.web.websocket;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisUtils;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.framework.web.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author admin
 * @Date 2024/9/28 0:45
 */
@Slf4j
@EnableScheduling
@Configuration
public class OrderRemindHandler implements WebSocketHandler {
    @Resource
    private TokenService tokenService;
    @Resource
    private RedisUtils redisUtils;

    /**用于存储已建立连接的用户*/
    public static Map<String, WebSocketSession> clients = new ConcurrentHashMap<>();

    @Scheduled(cron ="0/10 * * * * ?")
    public void orderRemind() {
        //获取指定长度的数据
        List<Object> rdList = redisUtils.rangeAnddel(RedisKeys.orderRemind, 0, 100);
        if (ObjectUtil.isNotNull(rdList) && !rdList.isEmpty()) {
            List<JSONObject> list = JSONUtil.toList(JSONUtil.toJsonStr(rdList), JSONObject.class);
            list.forEach(json -> {
                if (ObjectUtil.isNotNull(json.getLong("userId"))) {
                    WebSocketSession session = clients.get(json.getStr("userId"));
                    if (session != null) {
                        try {
                            log.info("websocket发送消息：{}", json);
                            session.sendMessage(new TextMessage(JSONUtil.toJsonStr(json)));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //建立连接后，根据用户ID将用户信息存入Map
        String token = session.getAttributes().get("Authorization").toString();
        LoginUser loginUser = tokenService.getLoginUser(token);
        if (loginUser != null && ObjectUtil.isNotNull(loginUser.getUserId())) {
            clients.put(loginUser.getUserId().toString(), session);
            log.info("用户连接连接{}", loginUser.getUsername());
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String token = session.getAttributes().get("Authorization").toString();
        LoginUser loginUser = tokenService.getLoginUser(token);
        if (loginUser != null && ObjectUtil.isNotNull(loginUser.getUserId())) {
            clients.remove(loginUser.getUserId().toString(), session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String token = session.getAttributes().get("Authorization").toString();
        LoginUser loginUser = tokenService.getLoginUser(token);
        if (loginUser != null && ObjectUtil.isNotNull(loginUser.getUserId())) {
            clients.remove(loginUser.getUserId().toString(), session);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
