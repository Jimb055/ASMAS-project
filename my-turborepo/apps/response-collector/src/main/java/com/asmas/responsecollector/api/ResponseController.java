package com.asmas.responsecollector.api;

import com.asmas.responsecollector.service.ResponseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/responses")
public class ResponseController {

    private final ResponseService service;

    public ResponseController(ResponseService service) {
        this.service = service;
    }

    @PostMapping("/{surveyId}")
    public ResponseEntity<Void> submitResponses(
            @PathVariable String surveyId,
            @RequestHeader("Authorization") String authorization,
            @RequestBody SubmitSurveyResponseRequest request
    ) {
        service.submitSurveyResponses(surveyId, authorization, request);
        return ResponseEntity.accepted().build();
    }
}

