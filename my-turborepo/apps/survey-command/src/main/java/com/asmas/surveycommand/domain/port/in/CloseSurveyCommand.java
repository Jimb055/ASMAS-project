package com.asmas.surveycommand.domain.port.in;

import java.util.UUID;

/**
 * Command to close an existing survey.
 */
public final class CloseSurveyCommand {

    private final UUID surveyId;
    private final String requestedBy;

    public CloseSurveyCommand(UUID surveyId, String requestedBy) {
        if (surveyId == null) {
            throw new IllegalArgumentException("surveyId cannot be null");
        }
        if (requestedBy == null || requestedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("requestedBy cannot be null or empty");
        }

        this.surveyId = surveyId;
        this.requestedBy = requestedBy;
    }

    public UUID getSurveyId() {
        return surveyId;
    }

    public String getRequestedBy() {
        return requestedBy;
    }
}
