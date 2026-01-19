variable "aws_region" {
  default = "us-east-1"
}

variable "instance_type" {
  default = "t3.micro"
}

variable "key_name" {
  description = "Key pair for auth-service"
}

variable "allowed_ssh_cidr" {
  description = "CIDR allowed for SSH"
}

variable "gateway_sg_id" {
  description = "Security group ID of API Gateway"
}
