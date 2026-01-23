output "public_ip" {
  value = aws_eip.this.public_ip
}

output "public_dns" {
  value = aws_eip.this.public_dns
}
