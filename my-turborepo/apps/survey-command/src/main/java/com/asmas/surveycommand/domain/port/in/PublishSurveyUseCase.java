package com.asmas.surveycommand.domain.port.in;

public interface PublishSurveyUseCase {

    PublishSurveyResponse execute(PublishSurveyCommand command);

}
