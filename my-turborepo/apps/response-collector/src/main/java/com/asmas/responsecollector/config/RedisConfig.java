package com.asmas.responsecollector.config;

import com.asmas.responsecollector.domain.ResponseRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, ResponseRecord> responseRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, ResponseRecord> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(ResponseRecord.class));
        template.afterPropertiesSet();
        return template;
    }
}