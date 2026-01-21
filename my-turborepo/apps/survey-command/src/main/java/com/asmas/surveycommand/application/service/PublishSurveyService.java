package com.asmas.surveycommand.application.service;

import com.asmas.surveycommand.domain.model.Survey;
import com.asmas.surveycommand.domain.port.in.PublishSurveyCommand;
import com.asmas.surveycommand.domain.port.in.PublishSurveyResponse;
import com.asmas.surveycommand.domain.port.in.PublishSurveyUseCase;
import com.asmas.surveycommand.domain.port.out.SurveyRepository;
import org.springframework.stereotype.Service;

@Service
public class PublishSurveyService implements PublishSurveyUseCase {

    private final SurveyRepository surveyRepository;

    public PublishSurveyService(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    @Override
    public PublishSurveyResponse execute(PublishSurveyCommand command) {

        Survey survey = surveyRepository.findById(command.getSurveyId())
                .orElseThrow(() -> new IllegalStateException("Survey not found"));

        // DOMAIN LOGIC
        survey.publish();

        surveyRepository.save(survey);

        return new PublishSurveyResponse(
                survey.getId(),
                survey.getStatus().name()
        );
    }
}
