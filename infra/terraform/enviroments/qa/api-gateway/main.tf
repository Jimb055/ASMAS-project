provider "aws" {
  region = var.region
}

resource "aws_security_group" "api_gateway_sg" {
  name        = "asmas-qa-api-gateway-sg"
  description = "Security group for QA API Gateway"

  ingress {
    description = "HTTP"
    from_port   = var.service_port
    to_port     = var.service_port
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "SSH (admin / bastion)"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.allowed_ssh_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "asmas-qa-api-gateway-sg"
  }
}

resource "aws_instance" "api_gateway" {
  ami                    = "ami-0c7217cdde317cfec" # Ubuntu 22.04 LTS
  instance_type          = var.instance_type
  key_name               = var.key_name
  vpc_security_group_ids = [aws_security_group.api_gateway_sg.id]

  tags = {
    Name        = "asmas-qa-api-gateway"
    Service     = "api-gateway"
    Environment = "QA"
  }
}

resource "aws_eip" "api_gateway_eip" {
  instance = aws_instance.api_gateway.id
}
