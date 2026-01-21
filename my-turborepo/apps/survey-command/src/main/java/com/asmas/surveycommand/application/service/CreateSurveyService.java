package com.asmas.surveycommand.application.service;

import org.springframework.stereotype.Service;

import com.asmas.surveycommand.domain.model.Survey;
import com.asmas.surveycommand.domain.port.in.CreateSurveyUseCase;
import com.asmas.surveycommand.domain.port.out.SurveyRepository;
import com.asmas.surveycommand.domain.port.in.CreateSurveyCommand;
import com.asmas.surveycommand.domain.port.in.CreateSurveyResponse;


@Service
public class CreateSurveyService implements CreateSurveyUseCase {
    private final SurveyRepository surveyRepository;

    public CreateSurveyService(SurveyRepository surveyRepository) {
        if (surveyRepository == null) {
            throw new IllegalArgumentException("SurveyRepository cannot be null");
        }
        this.surveyRepository = surveyRepository;
    }


    @Override
    public CreateSurveyResponse execute(CreateSurveyCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CreateSurveyCommand cannot be null");
        }

        // Create aggregate - will throw if invariants violated
        Survey survey = Survey.create(
                command.getTitle(),
                command.getDescription(),
                command.getCreatedBy()
        );
        
        // Persist via outbound port
        surveyRepository.save(survey);
        
        // Return command response (minimal data)
        return new CreateSurveyResponse(
                survey.getId(),
                survey.getTitle(),
                survey.getStatus().name()
        );
    }
}
