package com.asmas.surveycommand.domain.port.in;

import java.util.UUID;

public interface AddQuestionUseCase {

    void execute(AddQuestionCommand command);

    record AddQuestionCommand(
            UUID surveyId,
            String text
    ) {}


}
