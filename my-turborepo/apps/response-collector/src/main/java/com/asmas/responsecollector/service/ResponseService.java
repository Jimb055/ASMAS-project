package com.asmas.responsecollector.service;

import com.asmas.responsecollector.domain.ResponseRecord;
import com.asmas.responsecollector.repository.ResponseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ResponseService {

    private static final Logger log = LoggerFactory.getLogger(ResponseService.class);

    private final ResponseRepository repository;

    public ResponseService(ResponseRepository repository) {
        this.repository = repository;
    }

    public void submit(String surveyId, String questionId, String userId, String answer) {
        ResponseRecord record = new ResponseRecord(surveyId, questionId, userId, answer, Instant.now());
        repository.save(record);
        log.info("DomainEvent: ResponseSubmitted surveyId={} questionId={} userId={}", surveyId, questionId, userId);
    }
}