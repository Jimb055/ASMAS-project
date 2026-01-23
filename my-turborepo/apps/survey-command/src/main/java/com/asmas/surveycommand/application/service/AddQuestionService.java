package com.asmas.surveycommand.application.service;

import com.asmas.surveycommand.domain.model.Question;
import com.asmas.surveycommand.domain.model.Survey;
import com.asmas.surveycommand.domain.port.in.AddQuestionUseCase;
import com.asmas.surveycommand.domain.port.out.SurveyRepository;
import org.springframework.stereotype.Service;

@Service
public class AddQuestionService implements AddQuestionUseCase {

    private final SurveyRepository surveyRepository;

    public AddQuestionService(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    @Override
    public void execute(AddQuestionCommand command) {

        Survey survey = surveyRepository.findById(command.surveyId())
                .orElseThrow(() -> new IllegalStateException("Survey not found"));

        survey.addQuestion(new Question(command.text()));

        surveyRepository.save(survey);
    }
}

