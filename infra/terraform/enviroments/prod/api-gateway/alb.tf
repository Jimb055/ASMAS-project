########################################
# Data sources (VPC y Subnets)
########################################
data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "public" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

########################################
# Security Group del ALB
########################################
resource "aws_security_group" "alb_sg" {
  name        = "${var.environment}-${var.service_name}-alb-sg"
  description = "ALB SG for ${var.service_name}"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Si luego querés HTTPS, se agrega 443 aquí

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "${var.environment}-${var.service_name}-alb-sg"
    Environment = var.environment
    Service     = var.service_name
  }
}

########################################
# Application Load Balancer
########################################
resource "aws_lb" "this" {
  name               = "${var.environment}-${var.service_name}-alb"
  load_balancer_type = "application"
  internal           = false

  security_groups = [aws_security_group.alb_sg.id]
  subnets         = data.aws_subnets.public.ids

  tags = {
    Name        = "${var.environment}-${var.service_name}-alb"
    Environment = var.environment
    Service     = var.service_name
  }
}

########################################
# Target Group (Gateway 8080)
########################################
resource "aws_lb_target_group" "this" {
  name        = "${var.environment}-${var.service_name}-tg"
  port        = var.service_port
  protocol    = "HTTP"
  vpc_id      = data.aws_vpc.default.id
  target_type = "instance"

  health_check {
    path                = "/"
    protocol            = "HTTP"
    matcher             = "200"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 2
  }

  tags = {
    Name        = "${var.environment}-${var.service_name}-tg"
    Environment = var.environment
    Service     = var.service_name
  }
}

########################################
# Listener HTTP :80 -> Target Group
########################################
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.this.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.this.arn
  }
}


########################################
# Attach existing EC2 to Target Group
########################################
resource "aws_lb_target_group_attachment" "api_gateway_ec2" {
  target_group_arn = aws_lb_target_group.this.arn
  target_id        = aws_instance.this.id
  port             = var.service_port
}
