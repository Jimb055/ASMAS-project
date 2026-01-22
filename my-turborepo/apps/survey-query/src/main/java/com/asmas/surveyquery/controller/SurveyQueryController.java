package com.asmas.surveyquery.controller;

import com.asmas.surveyquery.model.SurveyReadModel;
import com.asmas.surveyquery.repository.SurveyReadRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
public class SurveyQueryController {

    private final SurveyReadRepository repository;

    public SurveyQueryController(SurveyReadRepository repository) {
        this.repository = repository;
    }

    // READ endpoints
    @GetMapping("/surveys")
    public List<SurveyReadModel> getAll() {
        return repository.findAll();
    }

    @GetMapping("/surveys/{id}")
    public ResponseEntity<SurveyReadModel> getById(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Internal sync endpoint (for read model updates only)
    @PostMapping("/internal/surveys")
    public ResponseEntity<Void> sync(@RequestBody SurveySyncRequest request) {
        var model = new SurveyReadModel(
                request.id(),
                request.title(),
                request.description(),
                request.status()
        );
        repository.upsert(model);
        return ResponseEntity.created(URI.create("/surveys/" + request.id())).build();
    }

    // DTO for internal sync
    public record SurveySyncRequest(String id, String title, String description, String status) {}
}