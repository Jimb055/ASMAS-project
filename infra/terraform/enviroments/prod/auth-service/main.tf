provider "aws" {
  region = var.aws_region
}

resource "aws_security_group" "this" {
  name        = "${var.environment}-${var.service_name}-sg"
  description = "SG for ${var.service_name}"

  # Auth service port
  ingress {
    from_port   = var.service_port
    to_port     = var.service_port
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # QA simplificado (antes: gateway_sg_id)
    description = "Auth service port"
  }

  # SSH
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.allowed_ssh_cidr]
    description = "SSH admin"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "${var.environment}-${var.service_name}-sg"
    Environment = var.environment
    Service     = var.service_name
  }
}

resource "aws_instance" "this" {
  ami                    = var.ami_id
  instance_type          = var.instance_type
  key_name               = var.key_name
  vpc_security_group_ids = [aws_security_group.this.id]

  user_data = <<-EOF
    #!/bin/bash
    set -e

    apt-get update -y
    apt-get install -y \
      ca-certificates \
      curl \
      gnupg \
      lsb-release \
      htop \
      unzip \
      jq \
      net-tools

    # Docker
    curl -fsSL https://get.docker.com | sh
    systemctl enable docker
    systemctl start docker
    usermod -aG docker ubuntu

    echo "Bootstrap completed for ${var.service_name}" > /etc/motd
  EOF

  tags = {
    Name        = "${var.environment}-${var.service_name}"
    Environment = var.environment
    Service     = var.service_name
  }
}

resource "aws_eip" "this" {
  instance = aws_instance.this.id

  tags = {
    Name        = "${var.environment}-${var.service_name}-eip"
    Environment = var.environment
    Service     = var.service_name
  }
}
