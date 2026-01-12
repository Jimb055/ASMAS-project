data "aws_ami" "amazon_linux_2" {
  most_recent = true
  owners      = ["amazon"]
  filter {
    name   = "name"
    values = ["amzn2-ami-hvm-*-x86_64-gp2"]
  }
}

resource "aws_launch_template" "lt_qa" {
  name_prefix   = "lt-qa-"
  image_id      = data.aws_ami.amazon_linux_2.id
  instance_type = "t3.micro"
  vpc_security_group_ids = [aws_security_group.ec2_qa.id]

  user_data = base64encode(<<-EOF
#!/bin/bash
set -xe

yum update -y
amazon-linux-extras install docker -y

systemctl enable docker
systemctl start docker

sleep 10

docker run -d -p 8080:80 nginx

EOF
  )
}

resource "aws_launch_template" "lt_prod" {
  name_prefix   = "lt-prod-"
  image_id      = data.aws_ami.amazon_linux_2.id
  instance_type = "t3.micro"
  vpc_security_group_ids = [aws_security_group.ec2_prod.id]

  user_data = base64encode(<<-EOF
#!/bin/bash
set -xe

yum update -y
amazon-linux-extras install docker -y

systemctl enable docker
systemctl start docker

sleep 10

docker run -d -p 8080:80 nginx
EOF
  )
}
