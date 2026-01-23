variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "environment" {
  type = string
}

variable "service_name" {
  type = string
}

variable "service_port" {
  type = number
}

variable "instance_type" {
  type    = string
  default = "t3.micro"
}

variable "ami_id" {
  type    = string
  default = "ami-0c7217cdde317cfec" # Ubuntu 22.04
}

variable "key_name" {
  type = string
}

variable "allowed_ssh_cidr" {
  type = string
}
