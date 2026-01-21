package com.asmas.surveycommand.domain.event;

import java.time.Instant;

public interface DomainEvent {

    Instant occurredAt();

}
