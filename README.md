# Distributed Academic Survey Data Management System (ASMAS)

## Project Overview
Academic project for Distributed Systems. The goal is to design and partially implement a production-like distributed system for survey management, response collection, and analytics under strict academic constraints.

## Architecture
- Microservices Architecture
- Event-Driven Architecture
- CQRS (Command / Query separation)
- Layered Architecture per microservice

## Technology Stack
- **Backend:** Java 17, Spring Boot / Spring Cloud
- **API Gateway:** Spring Cloud Gateway
- **Messaging:** Kafka or RabbitMQ
- **Datastores:**
  - PostgreSQL (Command side)
  - MongoDB (Query / Analytics)
  - Redis (Cache)
- **Observability:** Prometheus + Grafana
- **Automation:** n8n

## Monorepo Management
- Turborepo (v2.x)
- npm as package manager
- Structure:
  - `/apps` – microservices and gateway
  - `/packages` – shared libraries
  - `/infra/terraform` – infrastructure as code
  - `/docs` – architecture and execution evidence

## Branching Strategy
- **qa**: daily development and integration
- **prod**: final evaluated version (professor approval only)
- `main` branch is not used

## Commit Convention
Conventional Commits are mandatory, for example:
- `infra(terraform): ...`
- `build(docker): ...`
- `docs(architecture): ...`

## CI/CD
- GitHub Actions
- Automatic build and merge to **qa**
- Manual approval required for merge to **prod**

## Infrastructure (Terraform)
Provisioned on AWS (AWS Academy) using Terraform.

### Network
- Single VPC: `10.50.0.0/16`
- Public and private subnets across multiple AZs
- Internet Gateway for public access
- NAT Gateway for outbound traffic from private subnets
- Bastion Host for controlled SSH access

### Load Balancing & Compute (Day 8)
- Application Load Balancer (QA and PROD)
- Target Groups per environment
- Auto Scaling Groups using Launch Templates
- EC2 instances in private subnets
- Health checks validated using a placeholder nginx container

> **Note:** Private subnets use a dedicated route table with a default route to the NAT Gateway. This is required for ALB health checks, outbound access, and bastion connectivity.

## Current Status
- Architecture documentation completed
- Terraform base infrastructure completed
- ALB, ASG, and private subnet routing completed (Day 8)
- QA and PROD environments logically isolated

## Next Steps
- Deploy Spring Cloud Gateway behind the ALB (QA)
- Gradual deployment of command, query, and consumer microservices

