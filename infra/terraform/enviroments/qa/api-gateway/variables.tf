variable "region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.micro"
}

variable "key_name" {
  description = "Key pair for API Gateway EC2"
  type        = string
}

variable "allowed_ssh_cidr" {
  description = "Allowed CIDR for SSH access (Bastion or admin IP)"
  type        = string
}

variable "service_port" {
  description = "API Gateway service port"
  type        = number
  default     = 8080
}
