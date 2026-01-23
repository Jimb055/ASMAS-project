service_name     = "auth-service"
environment      = "prod"
instance_type    = "t3.micro"
service_port     = 8081
key_name         = "prod-auth-service-edge-key"
allowed_ssh_cidr = "179.49.51.26/32"

# Ubuntu 22.04 LTS - us-east-1
ami_id = "ami-0fc5d935ebf8bc3bc"
