package com.asmas.surveycommand.domain.model;

public class Question {
    private final String id;
    private final String text;
    private final String type;

    public Question(String id, String text, String type) {
        this.id = id;
        this.text = text;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }
}
