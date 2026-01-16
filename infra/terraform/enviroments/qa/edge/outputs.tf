output "QA_DNS_PUBLIC" {
  value = aws_instance.edge.public_dns
}

output "QA_STATIC_IP" {
  value = aws_eip.edge_eip.public_ip
}
