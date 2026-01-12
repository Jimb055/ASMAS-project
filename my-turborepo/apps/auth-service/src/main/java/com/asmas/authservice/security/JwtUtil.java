package com.asmas.authservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;


@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }
}

