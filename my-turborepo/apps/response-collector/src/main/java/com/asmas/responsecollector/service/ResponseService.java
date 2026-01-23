package com.asmas.responsecollector.service;

import com.asmas.responsecollector.api.SubmitSurveyResponseRequest;
import org.springframework.stereotype.Service;

@Service
public class ResponseService {

    public void submitSurveyResponses(
            String surveyId,
            String authorization,
            SubmitSurveyResponseRequest request
    ) {
        System.out.println("SurveyId: " + surveyId);
        System.out.println("Answers count: " + request.getAnswers().size());
    }
}
