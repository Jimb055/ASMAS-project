package com.asmas.surveycommand.domain.port.in;


public interface CreateSurveyUseCase {

    CreateSurveyResponse execute(CreateSurveyCommand command);

}
