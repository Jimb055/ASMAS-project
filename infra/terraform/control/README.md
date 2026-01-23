# Bastion Host Infrastructure

Terraform configuration for provisioning and managing the **Bastion Host** (jump host) for the ASMAS distributed system on AWS.

---

## Purpose of the Bastion Host

The **Bastion Host** is a hardened, single-entry-point EC2 instance that provides controlled SSH access to internal (private) EC2 instances running microservices. It acts as a security boundary between the public internet and the private application network.

### Why Use a Bastion Host?

1. **Network Isolation:** Private EC2 instances have no direct exposure to the internet; all SSH traffic is channeled through the bastion
2. **Centralized Access Control:** Single point for managing who can SSH into internal infrastructure
3. **Auditability & Compliance:** All SSH sessions can be logged and audited for security and debugging
4. **Attack Surface Reduction:** Reduces the number of instances exposed to the internet (only bastion is public)
5. **Simplified Security Group Management:** Private instances only need to allow SSH from bastion's security group, not from arbitrary external IPs

### Alternative Approaches (Not Used Here)

- **AWS Systems Manager Session Manager:** Requires EC2 instances to have IAM permissions and SSM agent; simpler but less transparent
- **VPN:** More complex; overkill for small academic team
- **Kubernetes:** Not applicable to this EC2-based architecture

---

## Bastion Host in QA & PROD Environments

The bastion is **shared** across both QA and PROD environments within the same VPC, but with environment-specific access controls.

### Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                  Internet / Developer PCs           │
└──────────────────────┬──────────────────────────────┘
                       │
        ┌──────────────▼──────────────┐
        │ Elastic IP: 54.123.45.67    │
        │ (Bastion Host - Public)     │
        │ Port 22 (SSH)               │
        └──────────────┬──────────────┘
                       │ SSH Tunnel
        ┌──────────────▼──────────────────────────────────────────┐
        │              VPC (10.50.0.0/16)                         │
        │                                                         │
        │  ┌────────────────────┬─────────────────────────────┐  │
        │  │   QA Environment   │   PROD Environment          │  │
        │  │                    │                             │  │
        │  │ Private Subnet     │ Private Subnet              │  │
        │  │ (10.50.0.0/24)     │ (10.50.1.0/24)              │  │
        │  │                    │                             │  │
        │  │ ┌────────────────┐ │ ┌──────────────────────┐   │  │
        │  │ │ api-gateway    │ │ │ api-gateway          │   │  │
        │  │ │ auth-service   │ │ │ auth-service         │   │  │
        │  │ │ survey-*       │ │ │ survey-*             │   │  │
        │  │ │ (SSH from      │ │ │ (SSH from Bastion    │   │  │
        │  │ │  Bastion only) │ │ │  only)               │   │  │
        │  │ └────────────────┘ │ └──────────────────────┘   │  │
        │  └────────────────────┴─────────────────────────────┘  │
        └─────────────────────────────────────────────────────────┘
```

### QA Environment Access

- **Bastion SSH Allowed From:** Team developer IPs, CI/CD server
- **Private EC2 SSH Allowed From:** Bastion security group only
- **Use Case:** Daily development, debugging, log inspection
- **Access Level:** Full SSH for all team members

### PROD Environment Access

- **Bastion SSH Allowed From:** Restricted admin IPs only (e.g., professor's network, lead developer)
- **Private EC2 SSH Allowed From:** Bastion security group only
- **Use Case:** Professor evaluation, post-evaluation debugging, audit trails
- **Access Level:** Limited; restricted to designated administrators

---

## Elastic IP for the Bastion Host

The Bastion Host is assigned an **Elastic IP (EIP)** — a static, persistent public IP address.

### Why Elastic IP?

1. **Predictable Address:** The bastion's public IP never changes, even if it's stopped/started
2. **Firewall/Network Rules:** Team members can configure their firewalls to allow traffic from the EIP
3. **DNS Records:** Can create a stable DNS name pointing to the EIP (e.g., `bastion.asmas.example.com`)
4. **Audit Trails:** Consistent IP makes SSH logs and access logs easier to trace
5. **Team Communication:** Easy to share with team: "SSH into 54.123.45.67" instead of dynamic IPs

### EIP Allocation & Association

```hcl
# Allocate an Elastic IP
resource "aws_eip" "bastion" {
  domain = "vpc"
  tags = {
    Name = "bastion-eip"
    Environment = "shared"
  }
}

# Associate EIP with Bastion EC2 instance
resource "aws_eip_association" "bastion" {
  instance_id      = aws_instance.bastion.id
  allocation_id    = aws_eip.bastion.id
}
```

### Cost Consideration

- **Elastic IP is free** while associated with a running instance
- **Charge ($0.005/hour)** applies only when EIP is allocated but NOT associated (idle)
- For academic project: Keep associated with running bastion to avoid charges

---

## Security Model & Access Flow

### Access Architecture

```
Developer Laptop (e.g., 192.168.1.100)
         │
         │ SSH Request (Port 22)
         │ Allowed if: source IP is in whitelist
         │
    ┌────▼────────────────────────────┐
    │ Bastion Host (54.123.45.67)     │
    │                                 │
    │ Security Group Rules:           │
    │  - Inbound: Port 22 from allowed IPs
    │  - Outbound: All (internal VPC) │
    └────┬─────────────────────────────┘
         │
         │ SSH Tunnel / Port Forward
         │ (within VPC, no internet routing)
         │
    ┌────▼──────────────────────────────────┐
    │ Private EC2 Instance (10.50.0.50)     │
    │ (e.g., api-gateway in QA)             │
    │                                       │
    │ Security Group Rules:                 │
    │  - Inbound: Port 22 from Bastion SG   │
    │  - No direct internet access          │
    └───────────────────────────────────────┘
```

### Step-by-Step Access Flow

1. **Developer Machine:** Initiates SSH connection to bastion Elastic IP (54.123.45.67)
2. **Internet:** Traffic routed to Bastion's public subnet
3. **Bastion Security Group:** Evaluates inbound rule
   - If developer's IP is in whitelist → SSH connection allowed
   - Otherwise → SSH connection denied
4. **Bastion EC2 Instance:** Receives SSH request, authenticates using SSH key
5. **Bastion OS:** User logs in; can now run commands or forward ports
6. **Port Forwarding (Optional):** Developer tunnels connections through bastion to private EC2
   ```bash
   ssh -i key.pem ubuntu@54.123.45.67 -L 8080:10.50.0.50:8080
   # Now localhost:8080 connects to private instance's port 8080
   ```
7. **Bastion → Private EC2:** SSH/tunnel connection initiated from bastion to private instance (10.50.0.50)
8. **Private EC2 Security Group:** Evaluates inbound rule
   - If source is bastion security group → SSH allowed
   - Otherwise → SSH denied
9. **Private EC2 OS:** User logs in (same SSH key); can inspect logs, debug, etc.

### Security Groups Configuration

**Bastion Security Group:**
```hcl
resource "aws_security_group" "bastion" {
  name_prefix = "bastion-"
  vpc_id      = aws_vpc.main.id

  # Inbound: SSH from allowed IPs
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = var.allowed_ssh_ips  # e.g., ["203.0.113.0/24", "198.51.100.0/24"]
  }

  # Outbound: All (for internal VPC communication)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "bastion-sg" }
}
```

**Private EC2 Security Group (QA/PROD):**
```hcl
resource "aws_security_group" "private_ec2" {
  name_prefix = "private-ec2-"
  vpc_id      = aws_vpc.main.id

  # Inbound: SSH only from Bastion security group
  ingress {
    from_port       = 22
    to_port         = 22
    protocol        = "tcp"
    security_groups = [aws_security_group.bastion.id]  # Only from bastion
  }

  # Inbound: Application port from ALB
  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]  # Only from ALB
  }

  tags = { Name = "private-ec2-sg" }
}
```

### SSH Key Management

- **Public Key:** Stored on Bastion EC2 instance (`~/.ssh/authorized_keys`)
- **Private Key:** Securely stored on developer machine (e.g., `~/.ssh/asmas-key.pem`)
- **Permissions:** Private key must be readable only by owner (`chmod 600`)
- **Distribution:** Keys shared securely via 1Password, Vault, or similar (not in Git)

---

## Bastion Configuration Details

### EC2 Instance Specifications

```hcl
resource "aws_instance" "bastion" {
  ami                         = data.aws_ami.ubuntu.id
  instance_type              = "t3.micro"  # Small, cost-optimized
  subnet_id                  = aws_subnet.public.id
  vpc_security_group_ids     = [aws_security_group.bastion.id]
  associate_public_ip_address = true

  # IAM role for minimal permissions
  iam_instance_profile       = aws_iam_instance_profile.bastion.name

  # User data: minimal setup
  user_data = base64encode(templatefile("${path.module}/bastion_user_data.sh", {
    region = var.aws_region
  }))

  tags = {
    Name        = "bastion-host"
    Environment = "shared"
  }
}
```

### Minimal IAM Role

The Bastion Host needs minimal IAM permissions (principle of least privilege):

```hcl
resource "aws_iam_role" "bastion" {
  name = "bastion-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "ec2.amazonaws.com"
      }
      Action = "sts:AssumeRole"
    }]
  })
}

# Optional: CloudWatch Logs for SSH session logging
resource "aws_iam_role_policy" "bastion_logs" {
  name = "bastion-logs"
  role = aws_iam_role.bastion.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents",
        "logs:DescribeLogStreams"
      ]
      Resource = "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/ec2/bastion:*"
    }]
  })
}
```

### User Data Script

Minimal bastion setup:
```bash
#!/bin/bash
# bastion_user_data.sh

set -e

# Update system
apt-get update && apt-get upgrade -y

# Install basic utilities
apt-get install -y \
  curl \
  wget \
  git \
  jq \
  awscli \
  fail2ban  # Rate-limit SSH attempts

# Enable fail2ban for brute-force protection
systemctl enable fail2ban
systemctl start fail2ban

# Configure SSH hardening (optional)
sed -i 's/#PermitRootLogin yes/PermitRootLogin no/' /etc/ssh/sshd_config
sed -i 's/#PubkeyAuthentication yes/PubkeyAuthentication yes/' /etc/ssh/sshd_config

systemctl restart ssh

# (Optional) Enable session logging to CloudWatch
# aws logs create-log-group --log-group-name /aws/ec2/bastion --region ${region}
```

---

## Environment-Specific Access Control

### QA Environment

**Bastion SSH Access:**
- **Allowed IPs:** Team developer networks, CI/CD server (e.g., GitHub Actions runner IP)
- **Configuration:**
  ```hcl
  variable "allowed_ssh_ips_qa" {
    default = ["203.0.113.0/24", "198.51.100.5/32"]  # Example IPs
  }
  ```

**Private EC2 Access:**
- All team members can SSH via bastion for development and debugging
- Full access to logs, databases, application processes

### PROD Environment

**Bastion SSH Access:**
- **Allowed IPs:** Restricted admin IPs only
- **Configuration:**
  ```hcl
  variable "allowed_ssh_ips_prod" {
    default = ["198.51.100.10/32"]  # Single admin IP (e.g., professor)
  }
  ```

**Private EC2 Access:**
- Limited to authorized administrators
- All access logged and audited
- Read-only access to sensitive operations (no production data modifications)

---

## Academic Justification

### Why This Security Model?

1. **Demonstrates Layered Security:** Multiple security boundaries (internet → bastion → private EC2)
2. **Practical Experience:** Students learn real-world access control patterns used in production
3. **Auditability:** Clear separation of concerns; all access can be logged and reviewed
4. **Compliance:** Aligns with security best practices (least privilege, defense-in-depth)
5. **Cost-Effective:** Bastion approach is simple and works with AWS free tier (unlike VPN or managed services)

### Learning Outcomes

Students gain understanding of:
- **Network Segmentation:** Public vs. private subnets and their purposes
- **Security Groups:** Role in controlling traffic at instance level
- **SSH Access Control:** How security models prevent unauthorized access
- **Infrastructure Hardening:** Techniques to reduce attack surface
- **Audit & Compliance:** Importance of logging and monitoring access

### Production Parallels

The bastion pattern is used in production by:
- Financial institutions (banking systems)
- Healthcare providers (HIPAA compliance)
- Cloud service providers (AWS infrastructure)
- Government agencies (classified systems)

This academic setup mirrors real-world security practices students will encounter in professional roles.

---

## Monitoring & Logging

### SSH Session Logging (Optional)

Configure bastion to log all SSH sessions:

```bash
# On bastion instance
echo 'session required pam_exec.so /usr/local/bin/log-ssh-session.sh' >> /etc/pam.d/sshd
```

### CloudWatch Logs Integration

Bastion can write logs to CloudWatch for centralized monitoring:

```hcl
resource "aws_cloudwatch_log_group" "bastion" {
  name              = "/aws/ec2/bastion"
  retention_in_days = 30

  tags = {
    Name = "bastion-logs"
  }
}
```

### Metrics to Monitor

- SSH connection attempts (successful and failed)
- User login duration and frequency
- Failed authentication attempts (possible brute-force)
- System resource usage (CPU, memory, disk)

---

## Disaster Recovery & Backup

### AMI Backup

Periodically create an AMI snapshot of the bastion for quick recovery:

```hcl
resource "aws_ami_from_instance" "bastion_backup" {
  name            = "bastion-${formatdate("YYYY-MM-DD", timestamp())}"
  source_instance_id = aws_instance.bastion.id

  tags = {
    Name = "bastion-backup"
  }
}
```

### Terraform State

Bastion configuration is managed by Terraform; state must be backed up:
- State stored in S3 with versioning enabled
- DynamoDB table for state locking (prevents concurrent modifications)

---

## Next Steps

1. **Define Allowed SSH IPs:** Update `var.allowed_ssh_ips_qa` and `var.allowed_ssh_ips_prod`
2. **Generate SSH Key Pair:** Create and securely distribute `asmas-key.pem` to team members
3. **Provision Bastion:** Run `terraform apply -target=aws_instance.bastion`
4. **Test SSH Access:** Verify connectivity from allowed IPs
5. **Monitor Access:** Review CloudWatch logs for SSH activity
6. **Document Procedures:** Create runbooks for team (SSH connection, port forwarding, troubleshooting)

---

## References

- [AWS Security Best Practices](https://aws.amazon.com/security/best-practices/)
- [VPC Security Groups Documentation](https://docs.aws.amazon.com/vpc/latest/userguide/VPC_SecurityGroups.html)
- [SSH Key Pairs Documentation](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html)
- [Elastic IPs Documentation](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/elastic-ip-addresses-eip.html)
- [Bastion Host Pattern](https://docs.aws.amazon.com/quickstart/latest/bastion-host/welcome.html) (AWS QuickStart)