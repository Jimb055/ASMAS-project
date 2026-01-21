package com.asmas.surveycommand.domain.port.in;

/**
 * Immutable command object for creating a survey.
 * Part of the CQRS command model.
 */
public final class CreateSurveyCommand {

    private final String title;
    private final String description;
    private final String createdBy;

    public CreateSurveyCommand(String title, String description, String createdBy) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Command: title cannot be null or empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Command: description cannot be null or empty");
        }
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Command: createdBy cannot be null or empty");
        }

        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
