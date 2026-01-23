package com.asmas.responsecollector.repository;

import com.asmas.responsecollector.domain.ResponseRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ResponseRepository {

    private final RedisTemplate<String, ResponseRecord> redisTemplate;

    public ResponseRepository(RedisTemplate<String, ResponseRecord> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(ResponseRecord record) {
        String key = "survey:" + record.getSurveyId() + ":responses";
        redisTemplate.opsForList().rightPush(key, record);
    }
}