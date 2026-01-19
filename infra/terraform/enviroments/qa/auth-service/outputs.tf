output "qa_auth_private_ip" {
  value = aws_instance.auth_service.private_ip
}

output "qa_auth_public_dns" {
  value = aws_instance.auth_service.public_dns
}
