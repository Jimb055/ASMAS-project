package com.asmas.surveycommand.domain.port.in;

import java.util.UUID;

/**
 * Response DTO for CreateSurvey use case.
 * Minimal output for command execution.
 */
public final class CreateSurveyResponse {

    private final UUID surveyId;
    private final String title;
    private final String status;

    public CreateSurveyResponse(UUID surveyId, String title, String status) {
        this.surveyId = surveyId;
        this.title = title;
        this.status = status;
    }

    public UUID getSurveyId() {
        return surveyId;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }
}
