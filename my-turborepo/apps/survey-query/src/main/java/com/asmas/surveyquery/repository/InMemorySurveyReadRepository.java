package com.asmas.surveyquery.repository;

import com.asmas.surveyquery.model.SurveyReadModel;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemorySurveyReadRepository implements SurveyReadRepository {

    private final ConcurrentHashMap<String, SurveyReadModel> storage = new ConcurrentHashMap<>();

    @Override
    public List<SurveyReadModel> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<SurveyReadModel> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public void upsert(SurveyReadModel model) {
        storage.put(model.id(), model);
    }
}