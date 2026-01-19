output "survey_command_public_ip" {
  value = aws_instance.survey_command.public_ip
}

output "survey_command_public_dns" {
  value = aws_instance.survey_command.public_dns
}
