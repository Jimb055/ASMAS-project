package com.asmas.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                //  Disable CSRF
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                //  NO sessions
                .securityContextRepository(
                        org.springframework.security.web.server.context.NoOpServerSecurityContextRepository.getInstance()
                )

                // Public routes
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/auth/**",
                                "/actuator/**"
                        ).permitAll()

                        // Another JWT filters
                        .anyExchange().authenticated()
                )

                .build();
    }
}
