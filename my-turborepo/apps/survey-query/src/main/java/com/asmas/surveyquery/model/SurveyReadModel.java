package com.asmas.surveyquery.model;

// Simple read model (immutable)
public record SurveyReadModel(
        String id,
        String title,
        String description,
        String status
) {}