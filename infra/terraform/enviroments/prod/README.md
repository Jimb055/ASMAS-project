# Production (PROD) Environment Infrastructure

Terraform configuration for the **PROD (Production) environment** of the ASMAS distributed system on AWS.

---

## Purpose of the PROD Environment

The **PROD environment** is the final, evaluated deployment of the ASMAS microservices architecture. It serves as the definitive system for academic assessment and represents production-grade infrastructure design.

### Key Roles

1. **Professor Evaluation:** Final system evaluated by instructors; represents cumulative project work
2. **Production-Grade Architecture:** Demonstrates understanding of security, scalability, and high availability
3. **Data Preservation:** All evaluation data retained; no resets or destructive operations
4. **Audit Trail:** All access and changes logged for compliance and reproducibility
5. **Stability & Reliability:** System operates under consistent configuration; changes infrequent
6. **Documentation Evidence:** System behavior recorded and documented for academic submission
7. **Learning Demonstration:** Shows students' mastery of distributed systems principles, infrastructure design, and operational practices

---

## PROD vs. QA: Key Differences

The PROD environment is **fundamentally more restrictive and resilient** than QA, reflecting production best practices:

### Security Posture

| Security Aspect | QA | PROD |
|-----------------|----|----|
| **SSH Access** | All team members via Bastion | Restricted admins only via Bastion |
| **SSH Audit** | Basic logging | Detailed audit logs (who, when, what) |
| **Database Passwords** | In Terraform variables (plaintext) | AWS Secrets Manager (encrypted) |
| **API Access** | Direct port exposure + ALB | ALB only (no direct exposure) |
| **Internal Communication** | HTTP (unencrypted) | HTTPS (TLS) required |
| **Security Groups** | Permissive (faster testing) | Minimal least-privilege rules |
| **Bastion Access IPs** | Wide team network ranges | Single IP or narrow CIDR blocks |
| **Instance Hardening** | Basic OS updates | Hardened AMI + security patches |

### Operational Posture

| Operational Aspect | QA | PROD |
|--------------------|----|----|
| **Instance Sizing** | t3.small (cost) | t3.medium/large (resilience) |
| **Auto Scaling** | min=1, desired=2, max=3 | min=2, desired=3, max=5 |
| **Database Backup** | Manual snapshots | Automated daily snapshots |
| **Monitoring** | Basic CloudWatch | Detailed metrics + alarms |
| **Deployment** | Automatic on `qa` branch | Manual approval required |
| **Data Retention** | Can be reset anytime | Preserved; no destructive ops |
| **Failover Strategy** | Single instance acceptable | Multi-AZ redundancy |
| **Health Checks** | Basic ALB health checks | Advanced monitoring + custom metrics |

### Network Architecture

| Network Aspect | QA | PROD |
|----------------|----|----|
| **Public Service Ports** | Yes (8080, 5432 exposed) | No (only ALB external) |
| **Direct Instance IPs** | Accessible for debugging | Hidden behind ALB |
| **Bastion Elastic IP** | Same as QA | May differ for audit trail |
| **ALB Configuration** | Single listener | Multiple listeners + rules |
| **DNS Records** | Optional | Required + health-checked |
| **SSL/TLS Termination** | HTTP only | HTTPS at ALB |

---

## Repository Structure

```
prod/                              # PROD environment root
│
├── README.md                       # This file
│
├── analytics/                      # Analytics microservice (PROD)
│   ├── main.tf                    # EC2 instance(s) for analytics
│   ├── variables.tf               # Input variables (instance sizing)
│   ├── outputs.tf                 # Output: instance IPs, DNS names
│   ├── terraform.tfvars           # PROD-specific values
│   ├── terraform.tfstate          # Current PROD infrastructure state
│   └── .terraform.lock.hcl        # Provider version lock
│
├── api-gateway/                   # Spring Cloud API Gateway (PROD)
│   ├── main.tf                    # EC2 Auto Scaling Group
│   ├── alb.tf                     # ALB + listeners (HTTPS termination)
│   ├── variables.tf               # ALB config, target groups, SSL certs
│   ├── outputs.tf                 # Output: ALB DNS, certificate ARN
│   ├── terraform.tfvars           # PROD ALB configuration
│   └── terraform.tfstate
│
├── auth-service/                  # Authentication service (PROD)
│   ├── main.tf                    # EC2 Auto Scaling Group
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars
│
├── command-db/                    # PostgreSQL (Command DB - PROD)
│   ├── main.tf                    # EC2 instance (larger instance type)
│   ├── variables.tf               # DB sizing, backup config, security
│   ├── outputs.tf                 # DB endpoint, port
│   └── terraform.tfvars
│
├── event-db/                      # Event Store Database (PROD)
│   ├── main.tf                    # EC2 instance (event store)
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars
│
├── event-processor/               # Event processor service (PROD)
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars
│
├── gamification/                  # Gamification service (PROD)
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars
│
├── notification/                  # Notification service (PROD)
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars
│
├── query-db/                      # MongoDB (Query DB - PROD)
│   ├── main.tf                    # EC2 instance (larger instance type)
│   ├── variables.tf               # DB sizing, replica considerations
│   ├── outputs.tf
│   └── terraform.tfvars
│
├── reporting/                     # Reporting service (PROD)
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars
│
├── response-collector/            # Response collection service (PROD)
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars
│
├── survey-command/                # Survey command service (PROD)
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars
│
└── survey-query/                  # Survey query service (PROD)
    ├── main.tf
    ├── variables.tf
    ├── outputs.tf
    └── terraform.tfvars
```

---

## Security Hardening in PROD

### 1. Restricted SSH Access

**QA Approach (Permissive):**
```hcl
# QA: All team members can SSH
variable "allowed_ssh_cidrs" {
  default = ["203.0.113.0/24"]  # Team office network
}
```

**PROD Approach (Restrictive):**
```hcl
# PROD: Only designated administrators
variable "allowed_ssh_cidrs" {
  default = ["198.51.100.10/32"]  # Single IP (professor)
  description = "Only professor's machine can SSH to Bastion"
}
```

**Implementation:**
- Bastion security group only accepts SSH from restricted IPs
- All SSH sessions logged to CloudWatch
- Failed authentication attempts trigger alerts
- Bastion instance hardened with fail2ban rate limiting

### 2. Database Credential Management

**QA Approach (Plaintext in Code):**
```hcl
# command-db/terraform.tfvars (QA)
db_password = "dev_password_123"  # ⚠️ Not secure!
```

**PROD Approach (Encrypted Storage):**
```hcl
# command-db/terraform.tfvars (PROD)
db_password_secret_name = "asmas/prod/command-db/password"

# Retrieve from AWS Secrets Manager
data "aws_secretsmanager_secret_version" "db_password" {
  secret_id = data.aws_secretsmanager_secret.db_password.id
}

# EC2 instance retrieves via IAM role
resource "aws_instance" "command_db" {
  iam_instance_profile = aws_iam_instance_profile.prod_ec2.name
  # ... instance configuration ...
}
```

**Benefits:**
- Credentials never stored in plaintext
- Automatic credential rotation
- IAM-based access control
- Audit trail of credential access

### 3. Security Groups: Least Privilege

**QA Security Group (Permissive):**
```hcl
# QA: Allow broad access for testing
resource "aws_security_group" "qa_microservices" {
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]  # Open to world
  }
}
```

**PROD Security Group (Restrictive):**
```hcl
# PROD: Only allow traffic from ALB
resource "aws_security_group" "prod_microservices" {
  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]  # Only from ALB
    description     = "Application traffic from ALB only"
  }

  ingress {
    from_port       = 22
    to_port         = 22
    protocol        = "tcp"
    security_groups = [aws_security_group.bastion.id]  # SSH from Bastion
    description     = "SSH access via Bastion only"
  }

  # Explicit deny for direct internet access
  egress {
    from_port   = 0
    to_port     = 65535
    protocol    = "tcp"
    cidr_blocks = ["10.50.0.0/16"]  # Only VPC internal
    description = "Internal VPC traffic only"
  }
}
```

### 4. Network Isolation

**QA Approach:**
```
Internet
    ↓
Direct to EC2 port 8080
    ↓
Microservice
```

**PROD Approach:**
```
Internet
    ↓ HTTPS
Application Load Balancer (Elastic IP)
    ↓ HTTP (internal VPC)
Microservice (private subnet, no direct internet)
```

**Benefits:**
- Single entry point (ALB) is monitored and controlled
- Microservices hidden from direct internet exposure
- DDoS mitigation at ALB level
- SSL/TLS termination at edge

### 5. Instance Hardening

**PROD User Data Script (Enhanced):**
```bash
#!/bin/bash
set -e

# Security updates
apt-get update && apt-get upgrade -y
apt-get autoremove -y

# Install security tools
apt-get install -y \
  fail2ban \
  aide \
  auditd \
  curl \
  wget

# Configure fail2ban (rate-limit brute-force attempts)
systemctl enable fail2ban
systemctl start fail2ban

# Harden SSH
sed -i 's/#PermitRootLogin yes/PermitRootLogin no/' /etc/ssh/sshd_config
sed -i 's/#PubkeyAuthentication yes/PubkeyAuthentication yes/' /etc/ssh/sshd_config
sed -i 's/#X11Forwarding yes/X11Forwarding no/' /etc/ssh/sshd_config

# Enable SELinux or AppArmor (optional)
# systemctl enable apparmor

# Configure auditd for compliance logging
systemctl enable auditd
systemctl start auditd

# Disable IPv6 (if not needed)
# echo "net.ipv6.conf.all.disable_ipv6 = 1" >> /etc/sysctl.conf

# Apply kernel parameters
sysctl -p

systemctl restart ssh

echo "Instance hardening complete"
```

---

## Application Load Balancer (ALB) for External Access

### Purpose

The **ALB** serves as the **only external entry point** to PROD services:

```
Clients (Internet)
    ↓ HTTPS (Port 443)
ALB (54.123.45.68 - Elastic IP)
    ├─ Route: /api/* → API Gateway target group
    ├─ Route: /health → Health check response
    └─ Route: /* → 404 or specific service
        ↓ HTTP (Port 8080, internal VPC)
Microservices (Private Subnet)
```

### ALB Configuration in PROD

```hcl
# api-gateway/alb.tf (PROD)

# Create ALB
resource "aws_lb" "prod_alb" {
  name               = "asmas-prod-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = aws_subnet.public[*].id

  enable_deletion_protection = true  # Prevent accidental deletion
  enable_http2               = true
  enable_cross_zone_load_balancing = true

  tags = {
    Name        = "asmas-prod-alb"
    Environment = "prod"
  }
}

# Elastic IP for ALB (static, predictable)
resource "aws_eip" "alb" {
  domain  = "vpc"
  depends_on = [aws_internet_gateway.main]

  tags = {
    Name = "asmas-prod-alb-eip"
  }
}

# HTTPS Listener (SSL/TLS termination)
resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.prod_alb.arn
  port              = "443"
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS-1-2-2017-01"
  certificate_arn   = aws_acm_certificate.prod.arn  # ACM certificate

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.api_gateway.arn
  }
}

# HTTP Listener (redirect to HTTPS)
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.prod_alb.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type = "redirect"

    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

# Target Group for API Gateway
resource "aws_lb_target_group" "api_gateway" {
  name     = "asmas-prod-api-gateway"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = aws_vpc.main.id

  health_check {
    healthy_threshold   = 2
    unhealthy_threshold = 2
    timeout             = 5
    interval            = 30
    path                = "/actuator/health"
    matcher             = "200"
  }

  tags = {
    Name = "asmas-prod-api-gateway-tg"
  }
}

# Register EC2 instances with target group
resource "aws_lb_target_group_attachment" "api_gateway" {
  target_group_arn = aws_lb_target_group.api_gateway.arn
  target_id        = aws_instance.api_gateway.id
  port             = 8080
}
```

### ALB Listener Rules

```hcl
# Advanced routing rules (if needed)
resource "aws_lb_listener_rule" "api_routes" {
  listener_arn = aws_lb_listener.https.arn
  priority     = 1

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.api_gateway.arn
  }

  condition {
    path_pattern {
      values = ["/api/*"]
    }
  }
}

resource "aws_lb_listener_rule" "health_check" {
  listener_arn = aws_lb_listener.https.arn
  priority     = 2

  action {
    type = "fixed-response"

    fixed_response {
      content_type = "application/json"
      message_body = jsonencode({ status = "ok" })
      status_code  = "200"
    }
  }

  condition {
    path_pattern {
      values = ["/health"]
    }
  }
}
```

---

## High Availability & Resilience

### Auto Scaling for Resilience

**QA Auto Scaling (Cost-Optimized):**
```hcl
# QA: min=1 (cost), but single point of failure
resource "aws_autoscaling_group" "qa_api_gateway" {
  min_size         = 1
  desired_capacity = 2
  max_size         = 3
}
```

**PROD Auto Scaling (Resilience-Optimized):**
```hcl
# PROD: min=2 (always redundant)
resource "aws_autoscaling_group" "prod_api_gateway" {
  name                = "asmas-prod-api-gateway-asg"
  launch_template {
    id      = aws_launch_template.api_gateway.id
    version = "$Latest"
  }

  min_size         = 2  # Always at least 2 instances
  desired_capacity = 3  # Default to 3
  max_size         = 5  # Allow scaling to 5

  vpc_zone_identifier = aws_subnet.private[*].id  # Multi-AZ

  health_check_type         = "ELB"  # Use ALB health checks
  health_check_grace_period = 300    # 5-minute grace for startup

  tag {
    key                 = "Name"
    value               = "asmas-prod-api-gateway"
    propagate_at_launch = true
  }

  tag {
    key                 = "Environment"
    value               = "prod"
    propagate_at_launch = true
  }

  lifecycle {
    create_before_destroy = true  # Zero-downtime updates
  }
}

# Scaling policies
resource "aws_autoscaling_policy" "scale_up" {
  name                   = "asmas-prod-scale-up"
  scaling_adjustment     = 1
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 300
  autoscaling_group_name = aws_autoscaling_group.prod_api_gateway.name
}

resource "aws_autoscaling_policy" "scale_down" {
  name                   = "asmas-prod-scale-down"
  scaling_adjustment     = -1
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 300
  autoscaling_group_name = aws_autoscaling_group.prod_api_gateway.name
}

# CloudWatch alarms trigger scaling
resource "aws_cloudwatch_metric_alarm" "cpu_high" {
  alarm_name          = "asmas-prod-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "120"
  statistic           = "Average"
  threshold           = "70"
  alarm_actions       = [aws_autoscaling_policy.scale_up.arn]
}

resource "aws_cloudwatch_metric_alarm" "cpu_low" {
  alarm_name          = "asmas-prod-cpu-low"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = "5"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "300"
  statistic           = "Average"
  threshold           = "20"
  alarm_actions       = [aws_autoscaling_policy.scale_down.arn]
}
```

### Multi-AZ Deployment

**PROD Subnets Across Multiple AZs:**
```hcl
# api-gateway/main.tf

# Private subnets in multiple AZs
resource "aws_subnet" "private" {
  for_each = {
    "us-east-1a" = "10.50.0.0/25"
    "us-east-1b" = "10.50.0.128/25"
  }

  vpc_id            = aws_vpc.main.id
  cidr_block        = each.value
  availability_zone = each.key

  tags = {
    Name = "asmas-prod-private-${each.key}"
  }
}

# EC2 instances distributed across AZs
resource "aws_instance" "api_gateway" {
  for_each = aws_subnet.private

  subnet_id              = each.value.id
  availability_zone      = each.value.availability_zone
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = "t3.medium"  # Larger than QA
  vpc_security_group_ids = [aws_security_group.prod_microservices.id]

  tags = {
    Name = "asmas-prod-api-gateway-${each.key}"
  }
}
```

### Database Backups

**Automated Daily Snapshots:**
```hcl
# command-db/main.tf

resource "aws_ebs_snapshot_schedule" "prod_command_db" {
  description = "Daily backup of PROD command database"

  create_rule {
    interval      = 24
    interval_unit = "HOURS"
    times         = ["03:00"]  # 3 AM UTC
  }

  retain_rule {
    count = 30  # Keep 30 daily backups
  }

  tags = {
    Name = "asmas-prod-command-db-backup"
  }
}
```

---

## Environment Configuration

### Variable Structure

PROD variables are more conservative and secure:

```hcl
# api-gateway/terraform.tfvars (PROD)

# Instance sizing: Larger than QA for reliability
instance_type = "t3.medium"  # vs. t3.small in QA

# Auto Scaling: Higher availability
min_size         = 2
desired_capacity = 3
max_size         = 5

# Network: Restricted access
allowed_ssh_cidrs = ["198.51.100.10/32"]  # Single admin IP

# Secrets: Retrieved from Secrets Manager
db_password_secret_name = "asmas/prod/command-db/password"

# Monitoring: More detailed
enable_detailed_monitoring = true
cloudwatch_log_retention   = 90  # 90 days vs. 30 in QA

# SSL/TLS: Required
enable_https = true
certificate_arn = "arn:aws:acm:us-east-1:123456789012:certificate/xxx"

# Tagging: Production designation
environment = "prod"
project     = "asmas"
tier        = "production"
```

### Production Tagging Strategy

```hcl
locals {
  prod_tags = {
    Environment     = "prod"
    Project         = "asmas"
    ManagedBy       = "terraform"
    BackupRequired  = "true"
    MonitoringLevel = "detailed"
    CostCenter      = "academic"
    DataClassification = "evaluation"
    Compliance      = "audit-required"
  }
}

resource "aws_instance" "api_gateway" {
  tags = merge(local.prod_tags, {
    Name    = "asmas-prod-api-gateway"
    Service = "api-gateway"
  })
}
```

---

## Monitoring & Observability

### CloudWatch Metrics

**PROD collects more detailed metrics:**

```hcl
# Enable detailed monitoring
resource "aws_instance" "prod_service" {
  monitoring = true  # 1-minute granularity vs. 5-minute for QA
}

# Custom metrics
resource "aws_cloudwatch_metric_alarm" "service_health" {
  alarm_name          = "asmas-prod-service-health"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "HealthyHostCount"
  namespace           = "AWS/ApplicationELB"
  period              = "60"
  statistic           = "Average"
  threshold           = "1"  # Alert if fewer than 1 healthy host
  alarm_actions       = [aws_sns_topic.prod_alerts.arn]
}
```

### Centralized Logging

```hcl
# All PROD services log to CloudWatch
resource "aws_cloudwatch_log_group" "prod_services" {
  for_each = toset([
    "api-gateway",
    "auth-service",
    "survey-command",
    "survey-query",
    # ... all services
  ])

  name              = "/aws/ec2/asmas-prod-${each.value}"
  retention_in_days = 90  # 3-month retention for compliance
}
```

### Alerting

```hcl
# SNS topic for PROD alerts
resource "aws_sns_topic" "prod_alerts" {
  name = "asmas-prod-alerts"
}

resource "aws_sns_topic_subscription" "prod_alerts_email" {
  topic_arn = aws_sns_topic.prod_alerts.arn
  protocol  = "email"
  endpoint  = "asmas-team@university.edu"
}

# Alarms send notifications
resource "aws_cloudwatch_metric_alarm" "database_cpu" {
  alarm_name          = "asmas-prod-db-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "3"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "300"
  statistic           = "Average"
  threshold           = "80"
  alarm_actions       = [aws_sns_topic.prod_alerts.arn]
}
```

---

## Deployment Workflow for PROD

### Prerequisites

1. QA environment is stable and validated
2. All tests pass on `qa` branch
3. Code reviewed and approved
4. Terraform state backed up
5. Team consensus on promoting to PROD

### Promotion Steps

```bash
# 1. Create PROD branch
git checkout -b promote-to-prod

# 2. Review PROD configuration
cd infra/terraform/environments/prod
terraform plan -var-file=terraform.tfvars -out=prod.plan

# 3. Submit for approval (PR review)
git push origin promote-to-prod
# Create PR; wait for professor/team approval

# 4. Merge to prod branch (after approval)
git checkout prod
git merge promote-to-prod

# 5. Apply Terraform changes
terraform apply prod.plan

# 6. Verify deployment
terraform output
curl https://api-prod.asmas.example.com/health
```

### Manual Approval Gates

```terraform
# Optional: Require manual approval before destroying
terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Prevent accidental destroys
  lifecycle {
    prevent_destroy = true  # Require explicit override to destroy
  }
}
```

---

## Intentional Simplifications & Academic Constraints

### What Would Be Different in Real Production

| Aspect | PROD (This Project) | Real Production |
|--------|---------------------|-----------------|
| **Database** | Single EC2 instance | AWS RDS or managed service |
| **Container Orchestration** | EC2 instances + Auto Scaling | Kubernetes (EKS) |
| **Service Mesh** | None | Istio or Linkerd for observability |
| **Multi-Region** | Single region (AWS Academy) | Active-active in multiple regions |
| **Disaster Recovery** | Manual snapshots | RTO/RPO guarantees |
| **Certificate Management** | Manual ACM certs | Automated renewal |
| **Infrastructure as Code** | Terraform | Terraform + GitOps (ArgoCD) |
| **Cost Optimization** | Acceptable for evaluation | Reserved instances, spot pricing |

### Why These Simplifications?

1. **AWS Academy Limitations:** Limited resources; managed services not available
2. **Time Constraints:** Complex setup (Kubernetes, service mesh) would exceed scope
3. **Learning Focus:** Emphasis on distributed systems architecture, not DevOps tooling
4. **Evaluability:** Simpler setup makes system behavior easier to understand and verify

---

## Disaster Recovery & Business Continuity

### Backup Strategy

```hcl
# Automated daily snapshots
resource "aws_backup_vault" "prod" {
  name = "asmas-prod-backup-vault"
}

resource "aws_backup_plan" "prod" {
  name = "asmas-prod-backup-plan"

  rule {
    rule_name       = "daily-backup"
    target_vault_name = aws_backup_vault.prod.name
    schedule        = "cron(0 3 ? * * *)"  # 3 AM UTC daily
    start_window    = 60
    completion_window = 120

    lifecycle {
      delete_after = 30  # Keep 30-day retention
    }
  }
}

resource "aws_backup_selection" "prod_resources" {
  name     = "asmas-prod-resources"
  plan_id  = aws_backup_plan.prod.id
  iam_role_arn = aws_iam_role.backup.arn

  resources = [
    aws_instance.command_db.arn,
    aws_instance.query_db.arn,
    # ... all critical resources
  ]
}
```

### Recovery Procedures (Documented)

**Recovery Time Objective (RTO):** < 1 hour
**Recovery Point Objective (RPO):** < 24 hours

Procedures documented separately (not in code) for team awareness.

---

## Compliance & Audit

### Change Log

All Terraform changes tracked via Git:

```bash
# Review who made what changes
git log --oneline infra/terraform/environments/prod/

# See specific changes to PROD configuration
git diff main..prod infra/terraform/environments/prod/
```

### Access Audit

```bash
# Who accessed PROD resources via SSH?
aws logs filter-log-events \
  --log-group-name /aws/ec2/bastion \
  --filter-pattern "ubuntu@10.50.1"  # PROD subnet
```

### State File Security

```hcl
# Remote state with encryption & locking
terraform {
  backend "s3" {
    bucket         = "asmas-terraform-state-prod"
    key            = "prod/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true  # SSE-S3 encryption
    dynamodb_table = "terraform-locks"
  }
}
```

---

## Next Steps for Professor Evaluation

1. **Access PROD System:** Use ALB DNS name (provided in outputs)
2. **Test API Gateway:** Submit HTTP requests via API Gateway
3. **Verify Data Persistence:** Create surveys in QA; verify they appear in analytics
4. **Review Infrastructure:** SSH to Bastion; inspect EC2 instances and databases
5. **Check Logs:** Review CloudWatch logs for system behavior
6. **Verify High Availability:** Manually terminate an instance; verify ALB automatically recovers
7. **Test Backup:** Restore from snapshot to verify data integrity
8. **Document Observations:** Record system behavior and architecture strengths/weaknesses

---

## References

- [AWS Production Best Practices](https://aws.amazon.com/architecture/well-architected/)
- [ALB Documentation](https://docs.aws.amazon.com/elasticloadbalancing/latest/application/)
- [Auto Scaling Documentation](https://docs.aws.amazon.com/autoscaling/)
- [AWS Security Best Practices](https://docs.aws.amazon.com/security/best-practices/)
- [Terraform Best Practices](https://www.terraform.io/cloud/guides/recommended-practices)
- [ASMAS QA Environment README](../qa/README.md)
- [ASMAS Infrastructure README](../README.md)
- [ASMAS Main README](../../../README.md)