package com.asmas.surveycommand.application.service;

import com.asmas.surveycommand.domain.model.Survey;
import com.asmas.surveycommand.domain.port.in.CloseSurveyCommand;
import com.asmas.surveycommand.domain.port.in.CloseSurveyResponse;
import com.asmas.surveycommand.domain.port.in.CloseSurveyUseCase;
import com.asmas.surveycommand.domain.port.out.SurveyRepository;
import org.springframework.stereotype.Service;

@Service
public class CloseSurveyService implements CloseSurveyUseCase {

    private final SurveyRepository surveyRepository;

    public CloseSurveyService(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    @Override
    public CloseSurveyResponse execute(CloseSurveyCommand command) {

        Survey survey = surveyRepository.findById(command.getSurveyId())
                .orElseThrow(() -> new IllegalStateException("Survey not found"));

        // Domain logic
        survey.close();

        surveyRepository.save(survey);

        return new CloseSurveyResponse(
                survey.getId(),
                survey.getStatus().name()
        );
    }
}
