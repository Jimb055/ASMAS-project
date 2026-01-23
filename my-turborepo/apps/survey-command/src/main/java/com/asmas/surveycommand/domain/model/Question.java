package com.asmas.surveycommand.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Question {

    private final String text;

    public Question(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Question text cannot be empty");
        }
        this.text = text.trim();
    }

    public String getText() {
        return text;
    }
}
