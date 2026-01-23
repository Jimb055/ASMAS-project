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

    @PostMapping
    public ResponseEntity<Void> submitResponse(
            @RequestHeader(name = "X-User-Id") String userId,
            @Valid @RequestBody SubmitResponseRequest request) {

        service.submit(request.getSurveyId(), request.getQuestionId(), userId, request.getAnswer());
        return ResponseEntity.accepted().build();
    }
}