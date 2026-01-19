provider "aws" {
  region = var.aws_region
}

resource "aws_security_group" "auth_sg" {
  name        = "asmas-qa-auth-sg"
  description = "Auth service security group"

  ingress {
    from_port       = 8081
    to_port         = 8081
    protocol        = "tcp"
    security_groups = [var.gateway_sg_id]
    description     = "From API Gateway"
  }

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
}

resource "aws_instance" "auth_service" {
  ami                    = "ami-0c7217cdde317cfec"
  instance_type          = var.instance_type
  key_name               = var.key_name
  vpc_security_group_ids = [aws_security_group.auth_sg.id]

  tags = {
    Name        = "asmas-qa-auth-service"
    Environment = "QA"
    Service     = "auth-service"
  }
  user_data = <<-EOF
    #!/bin/bash
    set -e

    apt-get update -y
    apt-get install -y ca-certificates curl gnupg lsb-release

    # Docker GPG key
    mkdir -p /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg

    # Docker repo
    echo \
    "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
    https://download.docker.com/linux/ubuntu \
    $(lsb_release -cs) stable" \
    > /etc/apt/sources.list.d/docker.list

    apt-get update -y
    apt-get install -y docker-ce docker-ce-cli containerd.io

    systemctl enable docker
    systemctl start docker

    usermod -aG docker ubuntu
    EOF

}
