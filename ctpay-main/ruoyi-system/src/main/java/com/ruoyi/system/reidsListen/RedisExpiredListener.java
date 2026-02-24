package com.ruoyi.system.reidsListen;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

public class RedisExpiredListener implements MessageListener {
 @Override
 public void onMessage(Message message, byte[] bytes) {
    byte[] body = message.getBody();// 建议使用: valueSerializer
    byte[] channel = message.getChannel();
    System.out.print("onMessage >> " );
    System.out.println(String.format("channel: %s, body: %s, bytes: %s"
                        ,new String(channel), new String(body), new String(bytes)));
  }

}