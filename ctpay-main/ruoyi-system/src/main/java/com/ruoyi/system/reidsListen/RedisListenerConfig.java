package com.ruoyi.system.reidsListen;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;


@Configuration
public class RedisListenerConfig {
 @Bean
 RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {

    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    //下面这种方式是灵活配置，针对每个库的失效key做处理
    //container.addMessageListener(new RedisExpiredListener(), new PatternTopic("__keyevent@0__:expired"));
    return container;
  }
}