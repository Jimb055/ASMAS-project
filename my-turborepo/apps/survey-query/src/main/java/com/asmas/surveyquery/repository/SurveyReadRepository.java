package com.asmas.surveyquery.repository;

import com.asmas.surveyquery.model.SurveyReadModel;

import java.util.List;
import java.util.Optional;

public interface SurveyReadRepository {
    List<SurveyReadModel> findAll();
    Optional<SurveyReadModel> findById(String id);
    void upsert(SurveyReadModel model); // internal sync only
}