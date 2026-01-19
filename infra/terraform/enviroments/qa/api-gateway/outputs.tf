output "qa_api_gateway_public_ip" {
  value       = aws_eip.api_gateway_eip.public_ip
  description = "Public IP of QA API Gateway"
}

output "qa_api_gateway_public_dns" {
  value       = aws_instance.api_gateway.public_dns
  description = "Public DNS of QA API Gateway"
}
