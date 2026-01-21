package com.asmas.surveycommand.domain.event;

import java.time.Instant;
import java.util.UUID;

public final class SurveyPublishedEvent implements DomainEvent {

    private final UUID surveyId;
    private final Instant occurredAt;

    public SurveyPublishedEvent(UUID surveyId) {
        this.surveyId = surveyId;
        this.occurredAt = Instant.now();
    }

    public UUID getSurveyId() {
        return surveyId;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
