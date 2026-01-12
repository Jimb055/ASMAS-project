# JWT Claims Propagation

## Overview
After validating JWT tokens, the API Gateway extracts selected claims and propagates them as HTTP headers.

## Propagated Headers
- X-Username
- X-User-Id

## Rationale
This approach avoids passing raw JWT tokens to downstream services while still providing user context.

## Current Limitations
- No role or permission propagation
- Headers are used only as contextual metadata
