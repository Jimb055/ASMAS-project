package com.asmas.responsecollector.domain;

import java.io.Serializable;
import java.time.Instant;

public class ResponseRecord implements Serializable {
    private String surveyId;
    private String questionId;
    private String userId;
    private String answer;
    private Instant submittedAt;

    public ResponseRecord() {}

    public ResponseRecord(String surveyId, String questionId, String userId, String answer, Instant submittedAt) {
        this.surveyId = surveyId;
        this.questionId = questionId;
        this.userId = userId;
        this.answer = answer;
        this.submittedAt = submittedAt;
    }

    public String getSurveyId() { return surveyId; }
    public void setSurveyId(String surveyId) { this.surveyId = surveyId; }

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
}