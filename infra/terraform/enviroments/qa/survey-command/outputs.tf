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
