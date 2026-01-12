resource "aws_lb_listener" "http_qa" {
  load_balancer_arn = aws_lb.alb_qa.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.tg_qa.arn
  }
}

resource "aws_lb_listener" "http_prod" {
  load_balancer_arn = aws_lb.alb_prod.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.tg_prod.arn
  }
}
