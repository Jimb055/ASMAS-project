package com.asmas.surveycommand.domain.event;

import java.time.Instant;
import java.util.UUID;

public final class SurveyCreatedEvent implements DomainEvent {

    private final UUID surveyId;
    private final String title;
    private final String createdBy;
    private final Instant occurredAt;

    public SurveyCreatedEvent(UUID surveyId, String title, String createdBy) {
        this.surveyId = surveyId;
        this.title = title;
        this.createdBy = createdBy;
        this.occurredAt = Instant.now();
    }

    public UUID getSurveyId() {
        return surveyId;
    }

    public String getTitle() {
        return title;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
