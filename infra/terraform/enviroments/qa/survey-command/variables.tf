variable "project" {
  type    = string
  default = "asmas"
}

variable "environment" {
  type    = string
  default = "qa"
}

variable "service_name" {
  type    = string
  default = "survey-command"
}

variable "instance_type" {
  type    = string
  default = "t3.micro"
}

variable "key_name" {
  type = string
}

variable "allowed_ssh_cidr" {
  type = string
}
