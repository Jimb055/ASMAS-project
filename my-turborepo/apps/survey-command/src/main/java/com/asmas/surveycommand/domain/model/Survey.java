package com.asmas.surveycommand.domain.model;

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

    private Survey(UUID id, String title, String description, SurveyStatus status,
                   List<Question> questions, LocalDateTime createdAt, String createdBy) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.questions = new ArrayList<>(questions);
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    /**
     * Factory method to create a new Survey aggregate.
     * Enforces initial invariants during creation.
     */
    public static Survey create(String title, String description, String createdBy) {
        validateTitle(title);
        validateDescription(description);
        validateCreatedBy(createdBy);

        return new Survey(
                UUID.randomUUID(),
                title.trim(),
                description.trim(),
                SurveyStatus.DRAFT,
                new ArrayList<>(),
                LocalDateTime.now(),
                createdBy
        );
    }

    /**
     * Command: Add a question to this survey.
     * Invariant: Can only add questions when status is DRAFT.
     */
    public void addQuestion(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("Question cannot be null");
        }
        if (this.status != SurveyStatus.DRAFT) {
            throw new IllegalStateException(
                    String.format("Cannot add questions to survey in %s status. Only DRAFT surveys can be modified.", 
                    this.status)
            );
        }
        this.questions.add(question);
    }

    /**
     * Command: Publish this survey.
     * Invariant: Survey must have at least one question and be in DRAFT status.
     */
    public void publish() {
        if (this.status != SurveyStatus.DRAFT) {
            throw new IllegalStateException(
                    String.format("Cannot publish survey in %s status. Only DRAFT surveys can be published.", 
                    this.status = SurveyStatus.PUBLISHED)
            );
        }
        if (this.questions.size() < MIN_QUESTIONS_TO_PUBLISH) {
            throw new IllegalStateException(
                    String.format("Cannot publish survey without questions. Minimum required: %d", 
                    MIN_QUESTIONS_TO_PUBLISH)
            );
        }
    }

    // Validation methods enforcing domain invariants
    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (title.length() < MIN_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Title must be at least %d characters long", MIN_TITLE_LENGTH)
            );
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Title must not exceed %d characters", MAX_TITLE_LENGTH)
            );
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (description.length() < MIN_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Description must be at least %d characters long", MIN_DESCRIPTION_LENGTH)
            );
        }
    }

    private static void validateCreatedBy(String createdBy) {
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("CreatedBy (userId) cannot be null or empty");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public SurveyStatus getStatus() {
        return status;
    }

    public List<Question> getQuestions() {
        return new ArrayList<>(questions);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
