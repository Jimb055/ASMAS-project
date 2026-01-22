variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "service_name" {
  type = string
}

variable "environment" {
  type    = string
  default = "qa"
}

variable "instance_type" {
  type    = string
  default = "t3.micro"
}

variable "service_port" {
  type = number
}

variable "key_name" {
  type = string
}

variable "allowed_ssh_cidr" {
  type = string
}

variable "ami_id" {
  type = string
}
