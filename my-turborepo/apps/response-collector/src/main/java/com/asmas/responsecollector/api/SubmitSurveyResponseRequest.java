package com.asmas.responsecollector.api;
import java.util.List;
public class SubmitSurveyResponseRequest {

    private List<AnswerDTO> answers;

    public List<AnswerDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerDTO> answers) {
        this.answers = answers;
    }
}
