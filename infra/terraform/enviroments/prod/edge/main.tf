terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# -----------------------------
# Security Group (EDGE)
# -----------------------------
resource "aws_security_group" "edge_sg" {
  name        = "${var.project_name}-${var.environment}-${var.node_role}-sg"
  description = "Security group for QA EDGE node"

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "App port"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# -----------------------------
# EC2 Instance (EDGE)
# -----------------------------
resource "aws_instance" "edge" {
  ami                    = "ami-0c7217cdde317cfec" # Amazon Linux 2023 us-east-1
  instance_type          = var.instance_type
  key_name               = var.key_name
  vpc_security_group_ids = [aws_security_group.edge_sg.id]

  user_data = <<-EOF
              #!/bin/bash
              yum update -y
              yum install -y docker
              systemctl enable docker
              systemctl start docker

              docker run -d -p 8080:80 nginx
              EOF

  tags = {
    Name = "${var.project_name}-${var.environment}-${var.node_role}"
  }
}

# -----------------------------
# Elastic IP
# -----------------------------
resource "aws_eip" "edge_eip" {
  instance = aws_instance.edge.id
}
