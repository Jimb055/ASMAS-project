package com.asmas.surveycommand.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Question {

    private final String text;
    private final List<String> options;

    public Question(String text, List<String> options) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Question text cannot be null or empty");
        }

        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Question must have at least one option");
        }

        this.text = text.trim();
        this.options = new ArrayList<>(options);
    }

    public String getText() {
        return text;
    }

    public List<String> getOptions() {
        return new ArrayList<>(options);
    }
}
