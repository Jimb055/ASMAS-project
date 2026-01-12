# API Gateway

## Purpose
The API Gateway acts as the single entry point for all client requests in the ASMAS system.

## Technology
- Spring Boot 3.2
- Spring Cloud Gateway
- Java 17

## Routing Configuration
The gateway uses static routing configuration.

| Path Prefix | Target Service | Port |
|------------|---------------|------|
| /auth/**   | Auth Service  | 8081 |

## Current Scope
- Request routing
- Path-based forwarding
- No authentication or authorization filters yet

## Verification
The following request was successfully executed through the gateway:

POST http://localhost:8080/auth/login

The request was forwarded to the Auth Service and a JWT token was returned.
