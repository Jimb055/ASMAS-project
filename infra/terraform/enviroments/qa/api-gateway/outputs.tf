output "service_name" {
  value = var.service_name
}

output "public_ip" {
  value = aws_eip.this.public_ip
}

output "public_dns" {
  value = aws_eip.this.public_dns
}

output "service_port" {
  value = var.service_port
}

output "ssh_command" {
  value = "ssh -i ~/.ssh/${var.key_name}.pem ubuntu@${aws_eip.this.public_ip}"
}

output "alb_dns_name" {
  value = aws_lb.this.dns_name
}

output "alb_arn" {
  value = aws_lb.this.arn
}

output "alb_target_group_arn" {
  value = aws_lb_target_group.this.arn
}
