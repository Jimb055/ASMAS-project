# JWT Integration Between Auth Service and API Gateway

## Overview
The ASMAS system uses JWT tokens for authentication, issued by the Auth Service and validated by the API Gateway.

## Responsibilities
- The Auth Service is responsible for generating and signing JWT tokens.
- The API Gateway validates JWT tokens before forwarding requests to protected routes.

## Shared Secret Requirement
Both services must use the same HMAC signing secret. Any mismatch results in token validation failure.

## Common Failure Case
During development, token validation initially failed due to mismatched JWT secrets between services.

## Resolution
The issue was resolved by aligning the shared secret configuration across the Auth Service and the API Gateway.

## Current Scope
- Token issuance
- Token validation at gateway level
- No role-based authorization
- No downstream service security yet
