resource "aws_lb" "alb_qa" {
  name               = "asmas-alb-qa"
  load_balancer_type = "application"
  internal           = false
  security_groups    = [aws_security_group.alb_qa.id]
  subnets            = [aws_subnet.public.id, aws_subnet.public_b.id]
  tags = {
    Name = "asmas-alb-qa"
  }
}

resource "aws_lb" "alb_prod" {
  name               = "asmas-alb-prod"
  load_balancer_type = "application"
  internal           = false
  security_groups    = [aws_security_group.alb_prod.id]
  subnets            = [aws_subnet.public.id, aws_subnet.public_b.id]
  tags = {
    Name = "asmas-alb-prod"
  }
}
