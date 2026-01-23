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

---

## High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Internet / Clients                        │
└─────────────────────┬───────────────────────────────────────┘
                      │
        ┌─────────────▼─────────────┐
        │  Cloudflare DNS / CDN     │
        │   (external routing)      │
        └─────────────┬─────────────┘
                      │
        ┌─────────────▼─────────────────────┐
        │  Application Load Balancer (ALB)  │
        │  (QA & PROD isolated)             │
        └─────────────┬─────────────────────┘
                      │
        ┌─────────────▼──────────────────────────────┐
        │  Spring Cloud API Gateway                  │
        │  (JWT validation, routing)                 │
        └─────────────┬──────────────────────────────┘
                      │
      ┌───────────────┼───────────────┐
      │               │               │
  ┌───▼───┐     ┌────▼────┐    ┌────▼────┐
  │ Auth  │     │ Survey  │    │ Response│
  │Service│     │ Command │    │Collector│
  └───┬───┘     └────┬────┘    └────┬────┘
      │              │              │
      └──────────────┼──────────────┘
                     │
        ┌────────────▼────────────┐
        │  Event Bus (Kafka)      │
        │  (async communication)  │
        └────────────┬────────────┘
                     │
      ┌──────────────┼──────────────┐
      │              │              │
  ┌───▼──────┐  ┌───▼───────┐  ┌──▼──────────┐
  │ Analytics│  │Notification│  │ Gamification│
  │ Consumer │  │ Consumer   │  │ Consumer    │
  └───┬──────┘  └───┬───────┘  └──┬──────────┘
      │             │             │
      └─────────────┼─────────────┘
                    │
    ┌───────────────┼────────────────┐
    │               │                │
┌───▼──┐     ┌─────▼──┐      ┌──────▼────┐
│ Query│     │Command  │      │ Reporting │
│  DB  │     │   DB    │      │    DB     │
│ (SQL)│     │  (SQL)  │      │(MongoDB)  │
└──────┘     └─────────┘      └───────────┘
```

**Key Architectural Principles:**
- **API Gateway:** Single entry point for all client requests; handles JWT validation and request routing
- **CQRS Separation:** Command side (PostgreSQL) handles writes; Query side (MongoDB) handles reads
- **Event-Driven:** Services communicate asynchronously via event bus (Kafka) to maintain loose coupling
- **Microservices:** Each service owns its domain logic and databases; independent deployment
- **Observability:** Prometheus scrapes metrics; Grafana provides dashboards

---

## Repository Structure

```
ASMAS-project/
├── my-turborepo/              # Monorepo root (primary workspace)
│   ├── apps/                  # Microservices & API Gateway
│   │   ├── api-gateway/       # Spring Cloud Gateway (Java/Maven)
│   │   ├── auth-service/      # Authentication & JWT (Java/Maven)
│   │   ├── survey-command/    # Survey creation & updates (CQRS Command)
│   │   ├── survey-query/      # Survey reads & search (CQRS Query)
│   │   ├── response-collector/# Collect responses from respondents
│   │   ├── event-processor/   # Event-driven business logic
│   │   ├── notifications/     # Notification delivery service
│   │   ├── gamification/      # Gamification & reward logic
│   │   ├── analytics/         # Analytics & data aggregation
│   │   └── reporting/         # Report generation
│   ├── packages/              # Shared libraries
│   │   ├── common-lib/        # Shared utilities & constants
│   │   ├── eslint-config/     # Linting rules
│   │   ├── typescript-config/ # TypeScript configurations
│   │   └── ui/                # Shared UI components (if applicable)
│   ├── infra/terraform/       # Infrastructure as Code (AWS provisioning)
│   │   ├── base/              # VPC, subnets, security groups, routing
│   │   ├── modules/           # Reusable Terraform modules
│   │   ├── enviroments/       # Environment-specific configs (QA & PROD)
│   │   └── control/           # State management & backend config
│   ├── docs/                  # Architecture & design documentation
│   ├── turbo.json             # Turborepo pipeline configuration
│   └── package.json           # Root package dependencies
├── infra/                     # Legacy/alternate infrastructure configs
├── .github/                   # GitHub Actions workflows & automation
│   ├── workflows/
│   │   └── ci.yml             # CI/CD pipeline definition
│   └── java-upgrade/          # Java dependency update logs
├── .config/
│   └── dotnet-tools.json      # (Optional) .NET tooling config
└── README.md                  # This file
```

**Key Workspace Details:**
- **Primary workspace:** `my-turborepo/` contains all active development
- **Terraform:** Located in `my-turborepo/infra/terraform/` for AWS resource provisioning
- **Builds:** Each Java service uses Maven (`pom.xml`); compiled artifacts in `target/` directories
- **Docker:** Each service has a `Dockerfile` for containerization; images deployed to EC2

---

## Environments: QA vs PROD

### QA Environment
- **Purpose:** Daily development, integration testing, and validation
- **Scope:** Shared infrastructure; rapid iteration
- **Deployment:** Automatic on successful CI pipeline
- **Database:** PostgreSQL and MongoDB (non-critical data)
- **Scale:** Single or small Auto Scaling Group
- **Access:** Team members via Bastion Host

### PROD Environment
- **Purpose:** Final evaluated version; academic assessment
- **Scope:** Logically isolated; professor evaluation only
- **Deployment:** Manual approval required (Conventional Commits + PR review)
- **Database:** PostgreSQL and MongoDB (academic evaluation data)
- **Scale:** Separate Auto Scaling Group; independent configuration
- **Access:** Restricted; Bastion Host for administrative access

**Isolation Strategy:**
- Separate VPC route tables and security group rules per environment
- Distinct target groups in ALB; traffic routed by hostname/path
- Independent Terraform state files per environment

---

## Infrastructure & Security Architecture

### Network Design
- **VPC:** `10.50.0.0/16` (single VPC for both environments)
- **Subnets:** Public and private across multiple Availability Zones (AZs)
- **Internet Gateway:** Allows ingress to public subnets
- **NAT Gateway:** Enables outbound internet access from private subnets (where EC2 instances run)
- **Bastion Host:** Hardened EC2 instance in public subnet for SSH access to private instances

### Load Balancing
- **Application Load Balancer (ALB):** 
  - Terminates HTTPS connections
  - Routes traffic to target groups based on hostname and path
  - Health checks validate service availability
  - Sits between internet and private EC2 instances

### Compute
- **EC2 Instances:** Deployed in private subnets; no direct internet exposure
- **Launch Templates:** Define instance configuration (AMI, security group, IAM role)
- **Auto Scaling Groups:** Automatically add/remove instances based on demand
- **IAM Roles:** Instances authenticated to access AWS services (S3, Secrets Manager, CloudWatch)

### Security Controls
- **Security Groups:** Restrict traffic by protocol, port, and source IP
  - ALB: Accepts port 443 (HTTPS) from internet
  - EC2: Accepts port 8080 (application) from ALB only; port 22 (SSH) from Bastion only
- **Bastion Host:** Single, audited entry point for SSH access; all team access logged
- **JWT Validation:** API Gateway validates tokens before routing to services
- **SSL/TLS:** CloudFlare or ALB terminates HTTPS; internal service-to-service communication over HTTP (within VPC)

### Routing
- **Public Route Table:** Default route (`0.0.0.0/0`) points to Internet Gateway
- **Private Route Table:** Default route (`0.0.0.0/0`) points to NAT Gateway
  - Allows EC2 instances to download packages, pull Docker images, reach external APIs
  - Required for ALB health checks from ALB subnet to EC2 subnet
  - Bastion SSH access flows through NAT if EC2 initiates outbound connection

---

## Service Deployment & Containerization

### Build Pipeline
1. **Local Development:** Developer commits code with Conventional Commits
2. **GitHub Actions (CI):** 
   - Triggered on push to `qa` branch
   - Runs tests, builds Docker images
   - Pushes images to Docker registry (ECR or Docker Hub)
   - Merges to `qa` if all checks pass
3. **Docker Image:** Each service containerized with runtime and dependencies
4. **Container Registry:** Images stored in AWS ECR or external registry

### Deployment to EC2
1. **Auto Scaling Group:** Launches new EC2 instances from Launch Template
2. **User Data Script:** Pulls latest Docker image and starts container on instance startup
3. **Service Discovery:** ALB health checks route traffic to healthy instances
4. **Monitoring:** CloudWatch collects logs and metrics from containers
5. **Scaling:** Auto Scaling Group adds/removes instances based on CPU/memory metrics

### Service Communication
- **Client → API Gateway:** HTTPS through ALB
- **API Gateway → Microservices:** HTTP within VPC (private subnets)
- **Service → Service:** Direct HTTP calls or async via Kafka
- **Service → Database:** Internal VPC route to managed databases (RDS, DocumentDB)

---

## Project Execution & Testing

### Quick Start (High Level)
1. **Infrastructure:** Run `terraform apply` in `my-turborepo/infra/terraform/` to provision AWS resources (VPC, ALB, ASG, EC2)
2. **Build Services:** `turbo build` to compile all microservices (Java → Docker images)
3. **Deploy to QA:** Push to `qa` branch; CI/CD automatically deploys to QA environment
4. **Test:** Access API Gateway endpoint; validate responses via Postman or similar
5. **Promote to PROD:** Manual PR approval and merge to `prod` branch for professor evaluation

### Local Development (Without AWS)
- Use Docker Compose locally to simulate microservices and messaging (Kafka)
- Connect to local PostgreSQL/MongoDB for development
- Test API via localhost:8080 (API Gateway)

### Testing Strategy
- **Unit Tests:** Per-service Maven tests
- **Integration Tests:** API Gateway routing and JWT validation
- **End-to-End:** Full survey lifecycle (create survey → collect responses → generate analytics)
- **Load Tests:** Validate ALB and Auto Scaling behavior under traffic

---

## Academic Scope & Trade-Offs

### Design Decisions
- **Monorepo Approach:** Simplified dependency management and CI/CD orchestration for academic project
- **Terraform Infrastructure:** IaC enables reproducible, auditable AWS provisioning
- **CQRS Pattern:** Separates read and write concerns; improves scalability and query performance
- **Event-Driven Communication:** Reduces coupling; enables independent service scaling
- **Bastion Host:** Provides secure, audited access to private resources without exposing them to internet

### Academic Constraints & Simplifications
- **Single VPC:** Production would use separate VPCs per environment; we use subnets for logical isolation
- **Managed Services:** RDS/DocumentDB not fully explored; local databases used initially
- **Limited Observability:** Basic Prometheus/Grafana setup; production would have advanced tracing and alerting
- **Security Hardening:** Authentication via JWT; production would add OAuth 2.0, rate limiting, DDoS protection
- **Cost Optimization:** AWS Academy free tier used; auto-scaling minimal to avoid unnecessary costs

### Lessons Learned & Future Improvements
- Infrastructure provisioning with Terraform provides auditability and reproducibility
- Microservices architecture enables independent scaling but adds operational complexity
- Event-driven communication decouples services but requires careful message schema management
- API Gateway simplifies client-side routing but requires careful performance tuning
- Bastion Host access works well for small teams; would need SSM Session Manager at scale

---

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

