output "bastion_public_dns" {
  value = aws_instance.bastion.public_dns
}

output "bastion_public_ip" {
  value = aws_eip.bastion.public_ip
}

output "bastion_ssh_command" {
  value = "ssh -i ${var.key_name}.pem ubuntu@${aws_eip.bastion.public_ip}"
}
