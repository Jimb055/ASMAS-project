output "public_dns" {
  value = aws_instance.node.public_dns
}

output "public_ip" {
  value = aws_instance.node.public_ip
}
