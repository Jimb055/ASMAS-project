package com.asmas.surveycommand.adapter.in.rest;

import com.asmas.surveycommand.domain.port.in.CreateSurveyUseCase;
import com.asmas.surveycommand.domain.port.in.PublishSurveyCommand;
import com.asmas.surveycommand.domain.port.in.PublishSurveyResponse;
import com.asmas.surveycommand.domain.port.in.PublishSurveyUseCase;
import com.asmas.surveycommand.domain.port.in.CloseSurveyCommand;
import com.asmas.surveycommand.domain.port.in.CloseSurveyResponse;
import com.asmas.surveycommand.domain.port.in.CloseSurveyUseCase;
import com.asmas.surveycommand.domain.port.in.CreateSurveyCommand;
import com.asmas.surveycommand.domain.port.in.CreateSurveyResponse;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/surveys")
public class SurveyController {
    private final CreateSurveyUseCase createSurveyUseCase;
    private final PublishSurveyUseCase publishSurveyUseCase;
    private final CloseSurveyUseCase closeSurveyUseCase;

    public SurveyController(CreateSurveyUseCase createSurveyUseCase, PublishSurveyUseCase publishSurveyUseCase, CloseSurveyUseCase closeSurveyUseCase) {
        if (createSurveyUseCase == null) {
            throw new IllegalArgumentException("CreateSurveyUseCase cannot be null");
        }
        if (publishSurveyUseCase == null) {
            throw new IllegalArgumentException("PublishSurveyUseCase cannot be null");
        }
        if (closeSurveyUseCase == null) {
            throw new IllegalArgumentException("CloseSurveyUseCase cannot be null");
        }
        this.createSurveyUseCase = createSurveyUseCase;
        this.publishSurveyUseCase = publishSurveyUseCase;
        this.closeSurveyUseCase = closeSurveyUseCase;
    }


    @PostMapping
    public ResponseEntity<CreateSurveyResponse> createSurvey(
            @RequestBody CreateSurveyRequest request,
            @RequestHeader("X-User-Id") String userId
    ) {
        // Validate HTTP input
        if (request == null) {
            throw new IllegalArgumentException("CreateSurveyRequest cannot be null");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("X-User-Id header is required");
        }

        try {
            // Translate HTTP to command
            CreateSurveyCommand command = new CreateSurveyCommand(
                    request.getTitle(),
                    request.getDescription(),
                    userId
            );
            
            // Execute command via inbound port
            CreateSurveyResponse response = createSurveyUseCase.execute(command);
            
            // Return command response
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            // Command validation failed
            throw ex; // Let Spring's exception handler convert to 400 Bad Request
        }
    }

    public static class CreateSurveyRequest {
        private String title;
        private String description;

        // Required for Jackson deserialization
        public CreateSurveyRequest() {}

        public CreateSurveyRequest(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }


    @PostMapping("/{id}/publish")
    public ResponseEntity<PublishSurveyResponse> publishSurvey(
            @PathVariable("id") UUID surveyId,
            @RequestHeader("X-User-Id") String userId
    ) {
        PublishSurveyCommand command =
                new PublishSurveyCommand(surveyId, userId);

        PublishSurveyResponse response =
                publishSurveyUseCase.execute(command);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/{id}/close")
    public ResponseEntity<CloseSurveyResponse> closeSurvey(
            @PathVariable("id") UUID surveyId,
            @RequestHeader("X-User-Id") String userId
    ) {
        CloseSurveyCommand command =
                new CloseSurveyCommand(surveyId, userId);

        CloseSurveyResponse response =
                closeSurveyUseCase.execute(command);

        return ResponseEntity.ok(response);
    }



}
