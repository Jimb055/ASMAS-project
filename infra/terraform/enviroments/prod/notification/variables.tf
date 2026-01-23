variable "service_name" {
  type        = string
  description = "Name of the microservice"
}

variable "environment" {
  type        = string
  description = "Environment name (qa)"
  default     = "qa"
}

variable "instance_type" {
  type    = string
  default = "t3.micro"
}

variable "service_port" {
  type        = number
  description = "Port exposed by the microservice"
}

variable "key_name" {
  type        = string
  description = "EC2 key pair name"
}

variable "allowed_ssh_cidr" {
  type        = string
  description = "CIDR allowed for SSH access"
}

variable "ami_id" {
  type        = string
  description = "Ubuntu 22.04 AMI ID"
}
