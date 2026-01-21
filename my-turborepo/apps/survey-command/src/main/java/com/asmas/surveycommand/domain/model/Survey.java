package com.asmas.surveycommand.domain.model;

import com.asmas.surveycommand.domain.event.DomainEvent;
import com.asmas.surveycommand.domain.event.SurveyClosedEvent;
import com.asmas.surveycommand.domain.event.SurveyCreatedEvent;
import com.asmas.surveycommand.domain.event.SurveyPublishedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Survey {

    private static final int MIN_TITLE_LENGTH = 3;
    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MIN_DESCRIPTION_LENGTH = 10;
    private static final int MIN_QUESTIONS_TO_PUBLISH = 1;

    private final UUID id;
    private final String title;
    private final String description;
    private SurveyStatus status;
    private final List<Question> questions;
    private final LocalDateTime createdAt;
    private final String createdBy;

    // 🔴 Domain Events buffer
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Survey(
            UUID id,
            String title,
            String description,
            SurveyStatus status,
            List<Question> questions,
            LocalDateTime createdAt,
            String createdBy
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.questions = new ArrayList<>(questions);
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    /* =========================
       FACTORY
       ========================= */

    public static Survey create(String title, String description, String createdBy) {
        validateTitle(title);
        validateDescription(description);
        validateCreatedBy(createdBy);

        Survey survey = new Survey(
                UUID.randomUUID(),
                title.trim(),
                description.trim(),
                SurveyStatus.DRAFT,
                new ArrayList<>(),
                LocalDateTime.now(),
                createdBy
        );

        // ✅ Emit domain event
        survey.domainEvents.add(
                new SurveyCreatedEvent(
                        survey.id,
                        survey.title,
                        survey.createdBy
                )
        );

        return survey;
    }

    /* =========================
       COMMANDS (DOMAIN BEHAVIOR)
       ========================= */

    public void addQuestion(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("Question cannot be null");
        }
        if (this.status != SurveyStatus.DRAFT) {
            throw new IllegalStateException(
                    "Cannot add questions unless survey is in DRAFT status"
            );
        }
        this.questions.add(question);
    }

    public void publish() {
        if (this.status != SurveyStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT surveys can be published"
            );
        }
        if (this.questions.size() < MIN_QUESTIONS_TO_PUBLISH) {
            throw new IllegalStateException(
                    "Survey must have at least one question to be published"
            );
        }

        this.status = SurveyStatus.PUBLISHED;

        // ✅ Emit domain event
        this.domainEvents.add(
                new SurveyPublishedEvent(this.id)
        );
    }

    public void close() {
        if (this.status != SurveyStatus.PUBLISHED) {
            throw new IllegalStateException(
                    "Only PUBLISHED surveys can be closed"
            );
        }

        this.status = SurveyStatus.CLOSED;

        // ✅ Emit domain event
        this.domainEvents.add(
                new SurveyClosedEvent(this.id)
        );
    }

    /* =========================
       DOMAIN EVENTS API
       ========================= */

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    /* =========================
       VALIDATIONS
       ========================= */

    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (title.length() < MIN_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "Title must be at least " + MIN_TITLE_LENGTH + " characters"
            );
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "Title must not exceed " + MAX_TITLE_LENGTH + " characters"
            );
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (description.length() < MIN_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(
                    "Description must be at least " + MIN_DESCRIPTION_LENGTH + " characters"
            );
        }
    }

    private static void validateCreatedBy(String createdBy) {
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("createdBy cannot be null or empty");
        }
    }

    /* =========================
       GETTERS (NO SETTERS)
       ========================= */

    public UUID getId() {
        return id;
    }

    public SurveyStatus getStatus() {
        return status;
    }

    public List<Question> getQuestions() {
        return new ArrayList<>(questions);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
