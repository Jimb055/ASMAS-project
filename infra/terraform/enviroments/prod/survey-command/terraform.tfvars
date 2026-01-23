service_name      = "survey-command"
environment       = "prod"
instance_type     = "t3.micro"
service_port      = 8083
key_name          = "prod-survey-command-command-key"
allowed_ssh_cidr  = "50.17.238.204/32"

# Ubuntu 22.04 LTS (us-east-1)
ami_id = "ami-0fc5d935ebf8bc3bc"
