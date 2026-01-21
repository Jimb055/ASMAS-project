package com.asmas.surveycommand.adapter.in.rest;

import com.asmas.surveycommand.domain.port.in.CreateSurveyUseCase;
import com.asmas.surveycommand.domain.port.in.CreateSurveyCommand;
import com.asmas.surveycommand.domain.port.in.CreateSurveyResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/surveys")
public class SurveyController {
    private final CreateSurveyUseCase createSurveyUseCase;

    public SurveyController(CreateSurveyUseCase createSurveyUseCase) {
        if (createSurveyUseCase == null) {
            throw new IllegalArgumentException("CreateSurveyUseCase cannot be null");
        }
        this.createSurveyUseCase = createSurveyUseCase;
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

    /**
     * HTTP Request DTO for CreateSurvey command.
     * Immutable transfer object from adapter layer.
     */
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
}
