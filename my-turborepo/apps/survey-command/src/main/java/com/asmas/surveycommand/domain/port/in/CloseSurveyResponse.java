package com.asmas.surveycommand.domain.port.in;

import java.util.UUID;

/**
 * Response DTO for CloseSurvey command execution.
 */
public final class CloseSurveyResponse {

    private final UUID surveyId;
    private final String status;

    public CloseSurveyResponse(UUID surveyId, String status) {
        this.surveyId = surveyId;
        this.status = status;
    }

    public UUID getSurveyId() {
        return surveyId;
    }

    public String getStatus() {
        return status;
    }
}
