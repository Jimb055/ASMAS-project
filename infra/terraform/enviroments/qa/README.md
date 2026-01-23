# QA Environment Infrastructure

Terraform configuration for the **QA (Quality Assurance) environment** of the ASMAS distributed system on AWS.

---

## Purpose of the QA Environment

The **QA environment** is a fully functional, development-facing deployment of the ASMAS microservices architecture. It serves as the primary testing and validation ground before promotion to production.

### Key Roles

1. **Development & Integration Testing:** Team members deploy and test microservices in a shared, AWS-hosted environment
2. **Architecture Validation:** Verify that microservices communicate correctly, databases persist data, and event-driven workflows execute as designed
3. **CI/CD Target:** Automatic deployments trigger on successful builds; serves as continuous integration endpoint
4. **Debugging & Troubleshooting:** Developers can SSH into instances, inspect logs, and diagnose issues in real-time
5. **Demo Environment:** Stakeholders and professors can access the running system to evaluate functionality
6. **Documentation & Evidence:** Professor evaluation occurs here; system behavior is documented for academic assessment

---

## QA vs. PROD: Design Philosophy

### Why QA is More Permissive

The QA environment intentionally allows greater flexibility and direct access compared to PROD:

| Aspect | QA | PROD |
|--------|----|----|
| **SSH Access** | All team members via Bastion | Restricted admins only |
| **Direct Port Exposure** | Some services expose ports directly | Only ALB-routed traffic |
| **Database Passwords** | Stored in Terraform variables | Retrieved from Secrets Manager |
| **Logging** | Basic; logs stored locally | Centralized, audited logs |
| **Deployment** | Automatic on `qa` branch push | Manual approval required |
| **Scaling** | Minimal (cost optimization) | Higher availability targets |
| **Data Retention** | Temporary; can be reset | Preserved for evaluation |
| **Configuration Changes** | Frequent iterations | Frozen after evaluation |

### Reasoning

1. **Rapid Iteration:** Developers need fast feedback loops; permissive access accelerates development
2. **Cost Optimization:** AWS Academy free tier limited; QA uses minimal resources
3. **Educational Focus:** Students learn to compare security models (QA = flexible, PROD = strict)
4. **Testing Freedom:** Developers need to break things, reset databases, and retry without restrictions

---

## Repository Structure

```
qa/                                # QA environment root
│
├── README.md                       # This file
│
├── analytics/                      # Analytics microservice
│   ├── main.tf                    # EC2 instance for analytics
│   ├── variables.tf               # Input variables (instance type, tags)
│   ├── outputs.tf                 # Output values (instance IP, DNS)
│   ├── terraform.tfvars           # Variable values for QA
│   ├── terraform.tfstate          # Current infrastructure state
│   └── .terraform.lock.hcl        # Provider version lock
│
├── api-gateway/                   # Spring Cloud API Gateway
│   ├── main.tf                    # ALB + EC2 instances
│   ├── alb.tf                     # ALB listener rules & target groups
│   ├── variables.tf               # Input variables
│   ├── outputs.tf                 # Output: ALB DNS, target group ARN
│   ├── terraform.tfvars           # QA-specific values
│   └── terraform.tfstate
│
├── auth-service/                  # Authentication service
│   ├── main.tf                    # EC2 instance
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars
│
├── command-db/                    # PostgreSQL (Command Model DB)
│   ├── main.tf                    # EC2 instance with PostgreSQL
│   ├── variables.tf               # DB size, storage, security groups
│   ├── outputs.tf                 # DB endpoint, port
│   └── terraform.tfvars
│
├── event-db/                      # Event Store Database
│   ├── main.tf                    # EC2 instance with event storage
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars
│
├── event-processor/               # Event processor service
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars
│
├── gamification/                  # Gamification service
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars
│
├── notification/                  # Notification service
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars
│
├── query-db/                      # MongoDB (Query Model DB)
│   ├── main.tf                    # EC2 instance with MongoDB
│   ├── variables.tf               # DB configuration
│   ├── outputs.tf
│   └── terraform.tfvars
│
├── reporting/                     # Reporting service
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars
│
├── response-collector/            # Response collection service
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars
│
├── survey-command/                # Survey command service (CQRS)
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars
│
└── survey-query/                  # Survey query service (CQRS)
    ├── main.tf
    ├── variables.tf
    ├── outputs.tf
    └── terraform.tfvars
```

---

## Service-Per-EC2 Architecture

### Design Approach

Each microservice runs on its **own dedicated EC2 instance** (or small Auto Scaling Group). This approach is intentionally simple and reflects academic constraints:

```
QA Environment
├── EC2: api-gateway (t3.small, port 8080)
├── EC2: auth-service (t3.small, port 8080)
├── EC2: survey-command (t3.small, port 8080)
├── EC2: survey-query (t3.small, port 8080)
├── EC2: response-collector (t3.small, port 8080)
├── EC2: event-processor (t3.small, port 8080)
├── EC2: notifications (t3.small, port 8080)
├── EC2: gamification (t3.small, port 8080)
├── EC2: analytics (t3.small, port 8080)
├── EC2: reporting (t3.small, port 8080)
├── EC2: command-db (t3.medium, PostgreSQL, port 5432)
├── EC2: query-db (t3.medium, MongoDB, port 27017)
├── EC2: event-db (t3.medium, event store, port 5432)
└── [Additional services as needed]
```

### Why Service-Per-EC2?

1. **Simplicity:** Each service runs independently; no container orchestration complexity (no Kubernetes)
2. **Isolation:** Service failures don't cascade; one crashed service doesn't affect others
3. **AWS Academy Friendly:** No additional tools; simple EC2 + Docker Compose or systemd
4. **Debugging:** Direct SSH access to any service instance for troubleshooting
5. **Academic Learning:** Students understand the overhead of multi-instance architecture

### Comparison to Production Alternatives

| Approach | QA | PROD (Alternative) |
|----------|----|----|
| **Service-Per-EC2** | ✅ Used here | Container orchestration (Kubernetes) |
| **Docker Compose** | Possible (lightweight) | Not suitable for production |
| **Serverless (Lambda)** | Not applicable | Possible for stateless services |
| **Managed Services (ECS)** | Overkill for QA | Better for production |

---

## Communication Between Services

### Service Discovery

In QA, services discover each other using **private VPC hostnames**:

```
api-gateway (10.50.0.10:8080)
    ↓ HTTP
survey-command (10.50.0.20:8080)
    ↓ HTTP
PostgreSQL (10.50.0.30:5432)
```

Each EC2 instance is registered in Route53 or uses hardcoded private IPs:

```terraform
# In api-gateway/main.tf
variable "survey_command_endpoint" {
  default = "http://survey-command-qa.internal:8080"
}

# In survey-command/main.tf
resource "aws_route53_record" "survey_command" {
  zone_id = aws_route53_zone.private.zone_id
  name    = "survey-command-qa.internal"
  type    = "A"
  ttl     = 300
  records = [aws_instance.survey_command.private_ip]
}
```

### Asynchronous Communication (Kafka)

Services communicate asynchronously via **Kafka** for event-driven workflows:

```
survey-command (publishes event)
    ↓
Kafka Topic: survey.created
    ↓ (consumed by)
event-processor, notifications, gamification, analytics
```

Kafka runs on its own EC2 instance (or could be a managed service like AWS MSK).

---

## Database Strategy

### PostgreSQL (Command DB)

```terraform
# command-db/main.tf
resource "aws_instance" "command_db" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = "t3.medium"
  subnet_id              = aws_subnet.private.id
  vpc_security_group_ids = [aws_security_group.postgres.id]

  # User data: install PostgreSQL
  user_data = base64encode(file("${path.module}/postgres_setup.sh"))

  root_block_device {
    volume_size = 20  # 20 GB storage
    volume_type = "gp2"
  }

  tags = {
    Name        = "command-db-qa"
    Environment = "qa"
    Service     = "command-db"
  }
}
```

**Configuration:**
- **Port:** 5432
- **Allowed From:** All microservices in private subnet
- **Data:** Survey definitions, commands, audit logs
- **Backup:** Manual snapshots (not automated in QA)

### MongoDB (Query DB)

```terraform
# query-db/main.tf
resource "aws_instance" "query_db" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = "t3.medium"
  subnet_id              = aws_subnet.private.id
  vpc_security_group_ids = [aws_security_group.mongodb.id]

  user_data = base64encode(file("${path.module}/mongodb_setup.sh"))

  root_block_device {
    volume_size = 20
    volume_type = "gp2"
  }

  tags = {
    Name        = "query-db-qa"
    Environment = "qa"
    Service     = "query-db"
  }
}
```

**Configuration:**
- **Port:** 27017
- **Allowed From:** Query microservices (survey-query, analytics, reporting)
- **Data:** Denormalized survey data, analytics aggregations
- **Backup:** Manual snapshots

### Redis (Optional Cache)

If implemented, Redis runs on a separate EC2 instance:

```terraform
# redis/main.tf (if needed)
resource "aws_instance" "redis" {
  ami           = data.aws_ami.ubuntu.id
  instance_type = "t3.micro"
  # ...
}
```

---

## Environment Configuration

### Variable Structure

Each service has `terraform.tfvars` defining QA-specific values:

```hcl
# api-gateway/terraform.tfvars
instance_type = "t3.small"
instance_count = 2  # Auto Scaling Group with 2 instances
environment = "qa"

# Allowed inbound traffic (more permissive in QA)
allowed_ssh_cidrs = ["10.50.0.0/16"]  # Any in VPC
allowed_app_cidrs = ["0.0.0.0/0"]     # Any from internet (for testing)

# Database configuration
db_name = "asmas_qa"
db_user = "asmas_dev"
db_password = "dev_password_123"  # Not ideal; should use Secrets Manager

# Service discovery
kafka_endpoint = "kafka-qa.internal:9092"
postgres_endpoint = "command-db-qa.internal:5432"
mongodb_endpoint = "query-db-qa.internal:27017"
```

### Tagging Strategy

All QA resources are tagged for identification and cost tracking:

```hcl
tags = {
  Environment = "qa"
  Project     = "asmas"
  ManagedBy   = "terraform"
  Team        = "distributed-systems"
  CostCenter  = "academic"
}
```

---

## Security Posture in QA

### Intentionally Relaxed Security

QA security is **intentionally more relaxed** than PROD to accelerate development:

**What's Different:**

1. **Public Service Ports:** Some services expose ports directly to internet (e.g., api-gateway on port 8080)
2. **Permissive SSH:** All team members can SSH to any QA instance via Bastion
3. **Simple Passwords:** Database passwords stored in Terraform (not Secrets Manager)
4. **No SSL/TLS:** Internal service communication over HTTP (not HTTPS)
5. **Minimal Monitoring:** Basic CloudWatch; no advanced alerting

**What's Still Secure:**

- Security groups restrict traffic to necessary ports
- SSH keys required for EC2 access
- Private subnets for microservices (not directly exposed)
- Bastion Host for SSH access control

### QA Security Groups Example

```hcl
# QA: More permissive
resource "aws_security_group" "qa_microservices" {
  name_prefix = "qa-microservices-"

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]  # Open to world (for testing)
  }

  ingress {
    from_port       = 22
    to_port         = 22
    protocol        = "tcp"
    security_groups = [aws_security_group.bastion.id]  # SSH from Bastion
  }
}

# PROD: Restricted
resource "aws_security_group" "prod_microservices" {
  name_prefix = "prod-microservices-"

  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]  # Only from ALB
  }

  ingress {
    from_port       = 22
    to_port         = 22
    protocol        = "tcp"
    security_groups = [aws_security_group.bastion.id]  # SSH from Bastion only
  }
}
```

---

## Relationship Between QA and PROD

### Mirror Architecture

PROD mirrors QA's structure but with enhanced security and reliability:

```
QA Environment                       PROD Environment
├── api-gateway (1-2 instances)  →  ├── api-gateway (2-5 instances)
├── auth-service                 →  ├── auth-service
├── survey-*                     →  ├── survey-*
├── PostgreSQL (t3.medium)       →  ├── PostgreSQL (t3.large)
├── MongoDB (t3.medium)          →  ├── MongoDB (t3.large)
└── [Services]                   →  └── [Services]
```

### Promotion Workflow

```
Developer Code
    ↓ (Push to `qa` branch)
GitHub Actions CI/CD
    ↓ (Build, test, deploy)
QA Environment (Automatic)
    ↓ (Manual PR review + approval)
PROD Environment (Manual, `prod` branch)
    ↓
Professor Evaluation
```

### Configuration Differences

When promoting to PROD, Terraform variables change:

```diff
# api-gateway/terraform.tfvars

- instance_type = "t3.small"
+ instance_type = "t3.medium"

- instance_count = 2
+ instance_count = 3

- allowed_app_cidrs = ["0.0.0.0/0"]
+ allowed_app_cidrs = [aws_security_group.alb.id]  # Only ALB

- db_password = "dev_password_123"
+ db_password = var.prod_db_password  # From Secrets Manager
```

---

## AWS Academy Limitations

The QA environment design reflects AWS Academy free tier constraints:

### Constraints

1. **Limited Compute Hours:** ~40 hours/month per learner
   - **Impact:** Cannot run 10+ t3.small instances 24/7
   - **Solution:** Start/stop instances; run only during development hours

2. **No Managed Services:** RDS, DocumentDB, ElastiCache not available
   - **Impact:** Run PostgreSQL/MongoDB on EC2 instead of managed services
   - **Solution:** Manual database management; no automatic failover

3. **Limited Resources:** t3.micro/small/medium only
   - **Impact:** Limited scalability; cannot test high-load scenarios
   - **Solution:** Accept as academic constraint; adequate for demonstration

4. **Single AWS Region:** us-east-1 only
   - **Impact:** No multi-region testing
   - **Solution:** Document multi-region considerations in PROD planning

### Cost Optimization Strategies

```terraform
# Example: Stop EC2 instances during off-hours
resource "aws_instance" "survey_command" {
  # ... other config ...

  # Tag for automated shutdown
  tags = {
    AutoShutdown = "true"
    ShutdownTime = "20:00 UTC"
  }
}

# Lambda function (scheduled) to stop tagged instances
# Would be implemented via AWS Lambda or third-party automation
```

---

## Provisioning QA Environment

### Prerequisites

1. AWS account (AWS Academy Learner Lab)
2. Terraform installed locally
3. AWS credentials configured
4. SSH key pair created (`asmas-key.pem`)

### Provisioning Steps

```bash
# Initialize Terraform
cd infra/terraform/environments/qa
terraform init

# Review changes
terraform plan -var-file=terraform.tfvars

# Provision all QA services
terraform apply -var-file=terraform.tfvars

# Individual service provisioning (optional)
cd api-gateway
terraform apply -var-file=terraform.tfvars
```

### Post-Provisioning

```bash
# Get ALB DNS name
terraform output alb_dns_name

# Get EC2 instance IPs
terraform output survey_command_private_ip

# Test connectivity to microservices
curl http://<api-gateway-ip>:8080/health

# SSH to instance via Bastion
ssh -i ~/.ssh/asmas-key.pem ubuntu@<bastion-ip> -J bastion
ssh -i ~/.ssh/asmas-key.pem ubuntu@<survey-command-private-ip>
```

---

## Monitoring & Troubleshooting

### CloudWatch Logs

Each EC2 instance sends logs to CloudWatch:

```bash
# View logs for specific service
aws logs tail /aws/ec2/qa-survey-command --follow

# Search for errors
aws logs filter-log-events \
  --log-group-name /aws/ec2/qa-survey-command \
  --filter-pattern "ERROR"
```

### Service Health Checks

Monitor service availability via ALB health checks:

```bash
# Get target group health
aws elbv2 describe-target-health \
  --target-group-arn arn:aws:elasticloadbalancing:...
```

### Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| Service unreachable | EC2 not running | Check instance status; restart if needed |
| Database connection refused | PostgreSQL not running | SSH to instance; check service status |
| High latency | Small instance type | Scale up to t3.medium |
| Cost exceeding free tier | Too many instances running | Stop unused instances; consolidate services |

---

## Next Steps

1. **Provision Base Infrastructure:** VPC, subnets, security groups (done in parent directory)
2. **Deploy Each Service:** Run `terraform apply` for each service folder
3. **Verify Communication:** Test service-to-service connectivity
4. **Deploy Microservices:** Push Docker images; update EC2 instances
5. **Run End-to-End Tests:** Create surveys, collect responses, generate reports
6. **Document Findings:** Record system behavior for professor evaluation
7. **Prepare for PROD:** When QA is stable, promote to PROD environment

---

## References

- [AWS Academy Documentation](https://www.awsacademy.com/)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS EC2 User Guide](https://docs.aws.amazon.com/ec2/index.html)
- [Security Best Practices](https://docs.aws.amazon.com/security/best-practices/)
- [ASMAS Main README](../../../README.md)
- [Terraform Infrastructure README](../README.md)