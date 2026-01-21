package com.asmas.surveycommand.domain.port.in;

import java.util.UUID;

public final class PublishSurveyResponse {

    private final UUID surveyId;
    private final String status;

    public PublishSurveyResponse(UUID surveyId, String status) {
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
