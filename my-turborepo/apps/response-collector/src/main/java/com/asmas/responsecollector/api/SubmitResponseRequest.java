package com.asmas.responsecollector.api;

import jakarta.validation.constraints.NotBlank;

public class SubmitResponseRequest {
    @NotBlank
    private String surveyId;
    @NotBlank
    private String questionId;
    @NotBlank
    private String answer;

    public String getSurveyId() { return surveyId; }
    public void setSurveyId(String surveyId) { this.surveyId = surveyId; }

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}