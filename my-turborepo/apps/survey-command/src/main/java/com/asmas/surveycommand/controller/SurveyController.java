package com.asmas.surveycommand.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SurveyController {

    @GetMapping("/protected/survey/ping")
    public ResponseEntity<String> ping(
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(
                "Authenticated request from user=" + username + ", id=" + userId
        );
    }
}
