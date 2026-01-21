package com.asmas.surveycommand.application.service;

import com.asmas.surveycommand.domain.event.DomainEvent;
import com.asmas.surveycommand.domain.model.Survey;
import com.asmas.surveycommand.domain.port.in.PublishSurveyCommand;
import com.asmas.surveycommand.domain.port.in.PublishSurveyResponse;
import com.asmas.surveycommand.domain.port.in.PublishSurveyUseCase;
import com.asmas.surveycommand.domain.port.out.DomainEventPublisher;
import com.asmas.surveycommand.domain.port.out.SurveyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublishSurveyService implements PublishSurveyUseCase {

    private final SurveyRepository surveyRepository;
    private final DomainEventPublisher domainEventPublisher;

    public PublishSurveyService(
            SurveyRepository surveyRepository,
            DomainEventPublisher domainEventPublisher
    ) {
        this.surveyRepository = surveyRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public PublishSurveyResponse execute(PublishSurveyCommand command) {

        // 1️⃣ Load aggregate
        Survey survey = surveyRepository.findById(command.getSurveyId())
                .orElseThrow(() -> new IllegalStateException("Survey not found"));

        // 2️⃣ Execute domain behavior
        survey.publish();

        // 3️⃣ Persist updated state
        surveyRepository.save(survey);

        // 4️⃣ Collect and publish domain events
        List<DomainEvent> events = survey.pullDomainEvents();
        domainEventPublisher.publish(events);

        // 5️⃣ Return command response
        return new PublishSurveyResponse(
                survey.getId(),
                survey.getStatus().name()
        );
    }
}
