package com.asmas.authservice.controller;

import com.asmas.authservice.dto.LoginRequest;
import com.asmas.authservice.security.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping(
        value = "/login",
        consumes = "application/json",
        produces = "application/json"
    )
    public Map<String, String> login(@RequestBody LoginRequest request) {
        String token = jwtUtil.generateToken(request.getUsername());
        return Map.of("token", token);
    }
}
