# Survey Command Service Infrastructure

## Runtime
- Java 17
- Spring Boot
- Dockerized deployment

## Health Checks
The service exposes a lightweight `/health` endpoint used by the Application Load Balancer.

## Network Model
- Runs in private subnets
- Receives traffic only from the ALB
- Outbound traffic via NAT Gateway

## Readiness
The service is prepared for database and messaging integration in later stages.
