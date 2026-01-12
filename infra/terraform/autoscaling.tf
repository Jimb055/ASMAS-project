resource "aws_autoscaling_group" "asg_qa" {
  name                      = "asmas-asg-qa"
  max_size                  = 2
  min_size                  = 1
  desired_capacity          = 1
  health_check_type         = "ELB"
  vpc_zone_identifier       = [aws_subnet.private.id, aws_subnet.private_b.id]

  launch_template {
    id      = aws_launch_template.lt_qa.id
    version = "$Latest"
  }

  target_group_arns = [aws_lb_target_group.tg_qa.arn]
  tag {
    key                 = "Name"
    value               = "asmas-asg-qa"
    propagate_at_launch = true
  }
}

resource "aws_autoscaling_group" "asg_prod" {
  name                      = "asmas-asg-prod"
  max_size                  = 2
  min_size                  = 1
  desired_capacity          = 1
  health_check_type         = "ELB"
  vpc_zone_identifier       = [aws_subnet.private.id, aws_subnet.private_b.id]

  launch_template {
    id      = aws_launch_template.lt_prod.id
    version = "$Latest"
  }

  target_group_arns = [aws_lb_target_group.tg_prod.arn]
  tag {
    key                 = "Name"
    value               = "asmas-asg-prod"
    propagate_at_launch = true
  }
}
