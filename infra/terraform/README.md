# Terraform Infrastructure as Code

Infrastructure as Code (IaC) for the ASMAS (Distributed Academic Survey Management System) on AWS, managed using Terraform.

---

## Overview

This directory contains the complete Terraform configuration for provisioning and managing AWS infrastructure across two isolated environments: **QA (Quality Assurance)** and **PROD (Production)**.

The infrastructure follows an environment-based approach with clear separation of concerns:

- **Control Plane:** Terraform state management and backend configuration (ACTIVE)
- **QA Environment:** Development and testing infrastructure (ACTIVE)
- **PROD Environment:** Production-grade, evaluated infrastructure (ACTIVE)

---

## ⚠️ CRITICAL: Active vs. Legacy/Archived Structure

This repository contains **active** and **legacy** files. Understanding the distinction is essential for proper infrastructure management.

### ACTIVE Infrastructure (Currently Used)

The following directories contain the **current, active Terraform configuration** that should be used for all provisioning operations:

```
infra/terraform/
├── control/                       ✅ ACTIVE
│   ├── main.tf                    # Backend configuration (S3, Terraform Cloud)
│   ├── terraform.tfvars           # Backend-specific values
│   ├── variables.tf               # Backend variables
│   ├── outputs.tf                 # State outputs
│   ├── README.md                  # Control plane documentation
│   └── terraform.tfstate          # Backend configuration state
│
├── environments/                  ✅ ACTIVE
│   ├── qa/                        # QA environment (all services)
│   │   ├── README.md              # QA documentation
│   │   ├── analytics/             # Each service has its own directory
│   │   ├── api-gateway/
│   │   ├── auth-service/
│   │   ├── command-db/
│   │   ├── event-db/
│   │   ├── event-processor/
│   │   ├── gamification/
│   │   ├── notification/
│   │   ├── query-db/
│   │   ├── reporting/
│   │   ├── response-collector/
│   │   ├── survey-command/
│   │   └── survey-query/
│   │
│   └── prod/                      # PROD environment (all services)
│       ├── README.md              # PROD documentation
│       ├── analytics/
│       ├── api-gateway/
│       ├── auth-service/
│       ├── command-db/
│       ├── event-db/
│       ├── event-processor/
│       ├── gamification/
│       ├── notification/
│       ├── query-db/
│       ├── reporting/
│       ├── response-collector/
│       ├── survey-command/
│       └── survey-query/
│
└── modules/                       ✅ ACTIVE (Optional - Reusable components)
    ├── bootstrap/                 # Initial AWS setup
    ├── ec2-node/                  # Reusable EC2 template
    └── security/                  # Reusable security group module
```

### LEGACY/ARCHIVED Directories (Historical Reference Only)

The following directories are **NOT part of the active infrastructure**:

```
infra/terraform/                   ❌ LEGACY/ARCHIVED (Do NOT use)
├── base/                          # OLD: Shared VPC/networking (superseded by per-environment config)
│   ├── vpc.tf
│   ├── subnets.tf
│   ├── igw.tf
│   ├── nat.tf
│   ├── routes.tf
│   ├── security.tf
│   ├── sg_alb_ec2.tf
│   ├── variables.tf
│   ├── providers.tf
│   └── terraform.tfstate
│
├── alb.tf                         # ARCHIVED: Old ALB configuration
├── autoscaling.tf                 # ARCHIVED: Old Auto Scaling setup
├── bastion.tf                     # ARCHIVED: Old Bastion configuration
├── launch_templates.tf            # ARCHIVED: Old launch template
├── listeners.tf                   # ARCHIVED: Old listener rules
├── target_groups.tf               # ARCHIVED: Old target group config
└── README.md                      # (This file - explaining the transition)
```

### Why Legacy Files Exist

These archived files represent **earlier iterations** of the infrastructure design. They are kept in the repository for:

1. **Audit Trail:** Shows architectural decisions and evolution over time
2. **Historical Reference:** Documents what was tried and what didn't work
3. **Learning:** Demonstrates infrastructure refactoring and improvements
4. **Git History:** Provides version control lineage for traceability

**IMPORTANT:** Do NOT modify, use, or deploy from these legacy files. They are **read-only historical artifacts**.

---

## Directory Structure (Active Infrastructure Only)

```
infra/terraform/
│
├── control/                                # ✅ Terraform state management
│   ├── main.tf                            # Backend configuration (S3, DynamoDB)
│   ├── terraform.tfvars                   # Backend variable values
│   ├── variables.tf                       # Backend variables definition
│   ├── outputs.tf                         # Backend-related outputs
│   ├── README.md                          # Backend documentation
│   ├── terraform.tfstate                  # Backend configuration state
│   └── .terraform.lock.hcl                # Provider version lock
│
├── environments/
│   │
│   ├── qa/                                # ✅ QA Environment
│   │   ├── README.md                      # QA environment documentation
│   │   │
│   │   ├── analytics/                     # Analytics microservice
│   │   │   ├── main.tf
│   │   │   ├── variables.tf
│   │   │   ├── outputs.tf
│   │   │   ├── terraform.tfvars
│   │   │   └── terraform.tfstate
│   │   │
│   │   ├── api-gateway/                   # Spring Cloud API Gateway
│   │   │   ├── main.tf
│   │   │   ├── alb.tf
│   │   │   ├── variables.tf
│   │   │   ├── outputs.tf
│   │   │   ├── terraform.tfvars
│   │   │   └── terraform.tfstate
│   │   │
│   │   ├── auth-service/                  # Authentication service
│   │   ├── command-db/                    # PostgreSQL (Command DB)
│   │   ├── event-db/                      # Event Store
│   │   ├── event-processor/               # Event processor service
│   │   ├── gamification/                  # Gamification service
│   │   ├── notification/                  # Notification service
│   │   ├── query-db/                      # MongoDB (Query DB)
│   │   ├── reporting/                     # Reporting service
│   │   ├── response-collector/            # Response collector service
│   │   ├── survey-command/                # Survey command service
│   │   └── survey-query/                  # Survey query service
│   │
│   └── prod/                              # ✅ PROD Environment
│       ├── README.md                      # PROD environment documentation
│       │
│       ├── analytics/                     # Analytics microservice (PROD)
│       ├── api-gateway/                   # Spring Cloud API Gateway (PROD)
│       ├── auth-service/                  # Authentication service (PROD)
│       ├── command-db/                    # PostgreSQL (Command DB, PROD)
│       ├── event-db/                      # Event Store (PROD)
│       ├── event-processor/               # Event processor service (PROD)
│       ├── gamification/                  # Gamification service (PROD)
│       ├── notification/                  # Notification service (PROD)
│       ├── query-db/                      # MongoDB (Query DB, PROD)
│       ├── reporting/                     # Reporting service (PROD)
│       ├── response-collector/            # Response collector service (PROD)
│       ├── survey-command/                # Survey command service (PROD)
│       └── survey-query/                  # Survey query service (PROD)
│
├── modules/                               # ✅ Reusable Terraform Modules (Optional)
│   ├── bootstrap/                         # Initial AWS setup utilities
│   ├── ec2-node/                          # Standardized EC2 instance template
│   └── security/                          # Reusable security group definitions
│
├── .terraform.lock.hcl                    # Provider version lock file (root)
├── terraform.tfstate                      # Current state (root level)
├── terraform.tfstate.backup               # Previous state backup
│
├── ❌ ARCHIVED FILES (Legacy - Do NOT use)
├── base/                                  # OLD: Superseded by per-environment config
├── alb.tf                                 # ARCHIVED
├── autoscaling.tf                         # ARCHIVED
├── bastion.tf                             # ARCHIVED
├── launch_templates.tf                    # ARCHIVED
├── listeners.tf                           # ARCHIVED
├── target_groups.tf                       # ARCHIVED
│
└── README.md                              # This file
```

---

## Infrastructure Architecture Overview

### High-Level Design

Each environment (QA and PROD) is **self-contained and independent**. All infrastructure components are defined within each environment directory:

```
QA Environment (environments/qa/)          PROD Environment (environments/prod/)
├── api-gateway/                           ├── api-gateway/
│   ├── ALB configuration                  │   ├── ALB configuration
│   ├── Target groups                      │   ├── Target groups
│   └── EC2 instances                      │   └── EC2 instances (High Availability)
│                                          │
├── analytics/                             ├── analytics/
├── auth-service/                          ├── auth-service/
├── survey-command/                        ├── survey-command/
├── survey-query/                          ├── survey-query/
├── command-db/                            ├── command-db/
│   └── PostgreSQL EC2                     │   └── PostgreSQL EC2 (with backups)
├── query-db/                              ├── query-db/
│   └── MongoDB EC2                        │   └── MongoDB EC2 (with backups)
└── [All services]                         └── [All services]

                ↓
        Shared VPC & Networking
        (Managed separately - see Legacy Base/)
                ↓
        Bastion Host, Security Groups, Routes
```

### Environment Characteristics

| Aspect | QA | PROD |
|--------|----|----|
| **Purpose** | Development & Testing | Evaluation & Assessment |
| **Network Isolation** | 10.50.0.0/24 (private subnet) | 10.50.1.0/24 (private subnet) |
| **Instance Sizing** | t3.small (cost-optimized) | t3.medium/large (resilience) |
| **Auto Scaling** | min=1, desired=2, max=3 | min=2, desired=3, max=5 |
| **Security** | Permissive (faster iteration) | Restrictive (least privilege) |
| **Deployment** | Automatic on `qa` branch | Manual approval required |
| **SSH Access** | All team members | Restricted admins only |
| **Database Backups** | Manual | Automated daily |
| **Monitoring** | Basic CloudWatch | Detailed metrics + alarms |
| **ALB** | Simple routing | HTTPS + advanced rules |

---

## Active Components (Detailed)

### 1. Control Plane (`control/`)

**Purpose:** Terraform state management and backend configuration

**Manages:**
- S3 bucket for remote state storage
- DynamoDB table for state locking
- Backend provider configuration
- Terraform Cloud integration (if used)

**Key Files:**
- `main.tf` – Backend definition (S3, remote state, locking)
- `variables.tf` – Backend variables
- `terraform.tfvars` – Backend-specific values

**Usage:**

```bash
cd infra/terraform/control
terraform init
terraform plan
terraform apply
```

**Outputs:** Terraform state bucket name, lock table name, state file location

---

### 2. QA Environment (`environments/qa/`)

**Purpose:** Development and testing infrastructure

**Architecture:**
- **Permissive Security:** Faster iteration for developers
- **Cost-Optimized:** Smaller instances, minimal redundancy
- **Shared Services:** Single instances or 2-3 replicas
- **Automatic Deployment:** Changes deployed automatically from `qa` branch

**Service Structure:** Each service has its own Terraform configuration

```
environments/qa/SERVICE/
├── main.tf                # EC2 instance + launch template
├── variables.tf           # Input variables (instance type, security groups)
├── outputs.tf             # Output: instance IP, DNS, security group ID
├── terraform.tfvars       # QA-specific values
└── terraform.tfstate      # Service state
```

**Example: API Gateway**

```bash
cd environments/qa/api-gateway
terraform init
terraform plan -var-file=terraform.tfvars
terraform apply -var-file=terraform.tfvars
```

**Outputs:** Instance IP addresses, security group IDs, service endpoints

**Documentation:** See `environments/qa/README.md`

---

### 3. PROD Environment (`environments/prod/`)

**Purpose:** Production-grade infrastructure for professor evaluation

**Architecture:**
- **Restrictive Security:** Least privilege access control
- **High Availability:** Multi-instance deployments with Auto Scaling
- **Resilience:** Multi-AZ deployment, automated backups
- **Manual Deployment:** Requires explicit approval before changes

**Service Structure:** Mirrors QA; each service self-contained

```
environments/prod/SERVICE/
├── main.tf                # EC2 Auto Scaling Group or instance
├── variables.tf           # Input variables (instance sizing, backups)
├── outputs.tf             # Output: ALB DNS, target group ARN, instance IDs
├── terraform.tfvars       # PROD-specific values
└── terraform.tfstate      # Service state (encrypted)
```

**Example: API Gateway with High Availability**

```bash
cd environments/prod/api-gateway
terraform init
terraform plan -var-file=terraform.tfvars
terraform apply -var-file=terraform.tfvars
```

**Outputs:** ALB DNS name, target group ARNs, Auto Scaling Group size

**Documentation:** See `environments/prod/README.md`

---

### 4. Reusable Modules (`modules/`)

**Purpose:** Encapsulated, reusable Terraform components for consistency

**Available Modules:**

- **`bootstrap/`** – Initial AWS setup (S3 buckets, IAM roles, Secrets Manager)
- **`ec2-node/`** – Standardized EC2 instance template with Docker support
- **`security/`** – Reusable security group definitions

**Usage Example:**

```hcl
# In environments/qa/api-gateway/main.tf

module "ec2_instance" {
  source = "../../modules/ec2-node"
  
  instance_name = "api-gateway-qa"
  instance_type = "t3.small"
  ami_id        = data.aws_ami.ubuntu.id
  subnet_id     = aws_subnet.private.id
  security_groups = [aws_security_group.qa_services.id]
  
  tags = {
    Environment = "qa"
    Service     = "api-gateway"
  }
}
```

---

## Infrastructure Evolution: Why the Refactoring

### The Problem with Legacy Approach

The original monolithic structure (`base/`, root-level `.tf` files) created several operational challenges:

1. **State Explosion:** All infrastructure in single state file → difficult to manage
2. **Environment Mixing:** QA and PROD configurations intermingled → error-prone
3. **Scaling Issues:** Adding new services required modifying central configs
4. **Access Control:** Hard to restrict who could change what environment
5. **Dependency Hell:** All components tightly coupled

**Legacy Example:**
```
infra/terraform/
├── vpc.tf             # Single VPC config
├── alb.tf             # Single ALB (used by both QA & PROD)
├── bastion.tf         # Single Bastion
└── [Many tangled configs]
```

Problem: Changing ALB affects both environments; hard to know what breaks.

### The Solution: Environment-Based Structure

The current design separates concerns and enforces isolation:

```
infra/terraform/
├── control/           # Just state management
├── environments/
│   ├── qa/           # QA completely self-contained
│   └── prod/         # PROD completely self-contained
└── modules/          # Reusable, tested components
```

**Benefits:**

1. **Independent States:** Each environment has separate state file
2. **Clear Ownership:** Each service responsible for its own infrastructure
3. **Easy Scaling:** Add new service = create new directory + copy pattern
4. **Access Control:** Restrict who can modify QA vs. PROD separately
5. **Testing:** Test changes in QA before promoting to PROD

---

## Provisioning Workflow (Active Infrastructure)

### Prerequisites

1. AWS account (AWS Academy Learner Lab)
2. Terraform v1.0+ installed locally
3. AWS CLI configured with valid credentials
4. SSH key pair created (`asmas-key.pem`)

### Step 1: Initialize Control Plane

```bash
cd infra/terraform/control
terraform init
terraform apply
```

This creates the S3 bucket and DynamoDB table for remote state storage.

### Step 2: Provision QA Environment (Per Service)

```bash
# Example: Provision API Gateway service in QA
cd infra/terraform/environments/qa/api-gateway
terraform init
terraform plan -var-file=terraform.tfvars
terraform apply -var-file=terraform.tfvars

# Get outputs (e.g., instance IP)
terraform output
```

Repeat for each service (analytics, auth-service, survey-command, etc.):

```bash
for service in analytics auth-service command-db event-db event-processor \
               gamification notification query-db reporting response-collector \
               survey-command survey-query; do
  cd infra/terraform/environments/qa/$service
  terraform init
  terraform apply -var-file=terraform.tfvars
done
```

### Step 3: Provision PROD Environment (Per Service)

```bash
# Only when QA is stable and validated
cd infra/terraform/environments/prod/api-gateway
terraform init
terraform plan -var-file=terraform.tfvars
terraform apply -var-file=terraform.tfvars
```

Repeat for all services as needed.

### Accessing Resources

```bash
# Get QA API Gateway endpoint
cd infra/terraform/environments/qa/api-gateway
terraform output api_gateway_ip

# Get PROD ALB DNS name
cd infra/terraform/environments/prod/api-gateway
terraform output alb_dns_name

# SSH to instance via Bastion
ssh -i ~/.ssh/asmas-key.pem ubuntu@<bastion-ip> -J <private-ip>
```

---

## State Management

### Per-Environment State Files

Each environment and service maintains its own state:

```
environments/qa/api-gateway/terraform.tfstate
environments/qa/analytics/terraform.tfstate
environments/prod/api-gateway/terraform.tfstate
environments/prod/analytics/terraform.tfstate
# ... one state file per service per environment
```

**Benefits:**
- Isolated changes (QA changes don't affect PROD)
- Easier to troubleshoot (state is small and focused)
- Better security (restrict access per environment)

### Remote State (Recommended)

```hcl
# In each service's main.tf or backend config

terraform {
  backend "s3" {
    bucket         = "asmas-terraform-state"
    key            = "environments/qa/api-gateway/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "terraform-locks"
  }
}
```

### State Locking

Prevents concurrent modifications via DynamoDB:

```bash
# View locks
aws dynamodb scan --table-name terraform-locks

# Force unlock (only if necessary)
terraform force-unlock <LOCK_ID>
```

---

## Common Operations

### Viewing Infrastructure

```bash
# List all resources in QA API Gateway
cd environments/qa/api-gateway
terraform state list

# Inspect specific resource
terraform state show aws_instance.api_gateway
```

### Planning Changes

```bash
# Preview changes without applying
terraform plan -var-file=terraform.tfvars -out=changes.tfplan

# Apply the planned changes
terraform apply changes.tfplan
```

### Destroying Infrastructure

```bash
# Destroy QA API Gateway service
cd environments/qa/api-gateway
terraform destroy -var-file=terraform.tfvars

# Destroy specific resource only
terraform destroy -target=aws_instance.api_gateway \
  -var-file=terraform.tfvars
```

**Warning:** PROD resources may have `prevent_destroy = true` to prevent accidental deletion.

### Scaling Services

```bash
# Increase PROD API Gateway from 3 to 5 instances
cd environments/prod/api-gateway

# Edit terraform.tfvars
# Change: desired_capacity = 3 → desired_capacity = 5

# Apply
terraform apply -var-file=terraform.tfvars
```

---

## Troubleshooting

### Issue: "State Locked"

**Error:** `Error acquiring the state lock`

**Cause:** Another user is modifying the same state file.

**Solution:**
```bash
# Wait for other user to finish, or
aws dynamodb scan --table-name terraform-locks
terraform force-unlock <LOCK_ID>
```

### Issue: Provider Version Mismatch

**Error:** `Error: Failed to query available provider packages`

**Cause:** `.terraform.lock.hcl` specifies different version than available.

**Solution:**
```bash
terraform init -upgrade
```

### Issue: AWS Credentials Expired

**Error:** `InvalidUserID.Malformed` or `ExpiredToken`

**Cause:** AWS session expired or credentials invalid.

**Solution:**
```bash
# Reconfigure AWS credentials
aws configure

# Or refresh AWS Academy learner lab credentials
# (Log into AWS console to refresh session)
```

### Issue: EC2 Instance Not Running

**Symptom:** Instance appears in terraform state but not in AWS console.

**Cause:** Instance creation failed or was manually terminated.

**Solution:**
```bash
# Refresh state
terraform refresh

# Check actual state
terraform state show aws_instance.service

# Reapply to recreate
terraform apply -var-file=terraform.tfvars
```

---

## Best Practices

### 1. Always Plan Before Apply

```bash
terraform plan -var-file=terraform.tfvars -out=plan.tfplan
# Review carefully
terraform apply plan.tfplan
```

### 2. Use Separate State for Each Environment

**Never mix QA and PROD in same state file.**

Each service should have its own directory with isolated state:
```
environments/qa/api-gateway/terraform.tfstate  (separate)
environments/prod/api-gateway/terraform.tfstate (separate)
```

### 3. Version Lock All Providers

```hcl
terraform {
  required_version = ">= 1.0"
  
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"  # Pin major version
    }
  }
}
```

### 4. Tag All Resources

```hcl
locals {
  common_tags = {
    Project     = "asmas"
    Environment = var.environment  # "qa" or "prod"
    ManagedBy   = "terraform"
    Team        = "distributed-systems"
  }
}

resource "aws_instance" "service" {
  tags = merge(local.common_tags, {
    Name    = "api-gateway-${var.environment}"
    Service = "api-gateway"
  })
}
```

### 5. Validate Regularly

```bash
# Check syntax
terraform validate

# Format code
terraform fmt -recursive .

# Dry-run security checks
tfsec .
```

### 6. Document Configuration Changes

```bash
# Before making infrastructure changes
git add environments/qa/api-gateway/terraform.tfvars
git commit -m "chore: Increase QA api-gateway instance count to 3 for load testing"

# Then apply
terraform apply
```

---

## Next Steps

1. **Read Environment Documentation:**
   - `environments/qa/README.md` – QA details and characteristics
   - `environments/prod/README.md` – PROD details and hardening

2. **Provision QA First:** Start with development environment
   ```bash
   cd infra/terraform/environments/qa/api-gateway
   terraform init && terraform apply
   ```

3. **Validate Communication:** Test service-to-service connectivity

4. **Deploy Microservices:** Push Docker images; verify running services

5. **Provision PROD:** When QA is stable, promote to PROD

6. **Monitor & Maintain:** Use CloudWatch for observability

---

## References

### Infrastructure Documentation

- [QA Environment Details](./environments/qa/README.md)
- [PROD Environment Details](./environments/prod/README.md)
- [Control Plane / Backend Setup](./control/README.md)

### Official Documentation

- [Terraform Documentation](https://www.terraform.io/docs)
- [AWS Provider for Terraform](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS VPC Concepts](https://docs.aws.amazon.com/vpc/latest/userguide/)
- [AWS EC2 Documentation](https://docs.aws.amazon.com/ec2/)
- [AWS ALB Documentation](https://docs.aws.amazon.com/elasticloadbalancing/latest/application/)

### ASMAS Project Documentation

- [ASMAS Main README](../../README.md)
- [Microservices Architecture](../../my-turborepo/README.md)

### Best Practices & Learning

- [AWS Well-Architected Framework](https://aws.amazon.com/architecture/well-architected/)
- [Terraform Best Practices](https://www.terraform.io/cloud/guides/recommended-practices)
- [Infrastructure as Code Best Practices](https://www.terraform.io/docs/cloud/overview)

---

## Maintenance & Support

### Regular Checks

```bash
# Check Terraform version compatibility
terraform version

# Validate all configurations
terraform validate

# Format all files consistently
terraform fmt -recursive .

# Security scan (if tfsec installed)
tfsec .
```

### Getting Help

- **Error Messages:** Read carefully; they usually indicate the exact issue
- **AWS Console:** Check resource status when provisioning fails
- **CloudWatch Logs:** Review application logs for runtime issues
- **Git History:** `git log --oneline infra/terraform/` shows infrastructure evolution
- **State Files:** `terraform state show` displays current resource configuration

### Reporting Issues

If infrastructure provisioning fails:

1. Run `terraform plan` to identify the problem
2. Check AWS console for resource status
3. Review CloudWatch logs for errors
4. Document the error, context, and steps to reproduce
5. Check `.terraform.lock.hcl` for provider version issues

---

## Summary

**Active Infrastructure:**
- ✅ `control/` – Terraform state and backend
- ✅ `environments/qa/` – QA services (all 12+)
- ✅ `environments/prod/` – PROD services (all 12+)
- ✅ `modules/` – Reusable components

**Legacy/Archived (Do NOT use):**
- ❌ `base/` – Old VPC config
- ❌ Root-level `.tf` files (`alb.tf`, `autoscaling.tf`, etc.) – Legacy monolithic approach

**Next Action:** Begin provisioning QA environment following steps in `environments/qa/README.md`.