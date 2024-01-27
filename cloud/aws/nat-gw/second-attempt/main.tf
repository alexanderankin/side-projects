terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.34.0"
    }
    /*
    tls = {
      source = "hashicorp/tls"
      version = "4.0.5"
    }
    */
  }
}

provider "aws" {
  region = "us-east-1"
}

resource "aws_vpc" "vpc" {
  cidr_block           = "10.0.0.0/16" # [ '10.0.0.0', '10.0.255.255' ]
  enable_dns_hostnames = true
  enable_dns_support   = true
  tags                 = {
    "Name" = "Dummy"
  }
}

# aws ec2 describe-availability-zones --filters Name=region-name,Values=us-east-1 --query 'AvailabilityZones[*].ZoneName'
data "aws_availability_zones" "available" {}

resource "aws_subnet" "instance" {
  availability_zone = data.aws_availability_zones.available.names[0]
  cidr_block        = "10.0.1.0/24" # [ '10.0.1.0', '10.0.1.255' ]
  vpc_id            = aws_vpc.vpc.id
  tags              = {
    "Name" = "DummySubnetInstance"
  }
}

resource "aws_security_group" "securitygroup" {
  name        = "DummySecurityGroup"
  description = "DummySecurityGroup"
  vpc_id      = aws_vpc.vpc.id
  /*
  ingress {
    cidr_blocks = ["0.0.0.0/0"]
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
  }
  egress {
    cidr_blocks = ["0.0.0.0/0"]
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
  }
  */
  tags = {
    "Name" = "DummySecurityGroup"
  }
}

resource "aws_security_group_rule" "r1" {
  cidr_blocks       = ["0.0.0.0/0"]
  from_port         = 22
  to_port           = 22
  protocol          = "Tcp"
  type              = "ingress"
  security_group_id = aws_security_group.securitygroup.id
}

resource "aws_security_group_rule" "r2" {
  cidr_blocks       = ["0.0.0.0/0"]
  from_port         = 0
  to_port           = 0
  protocol          = "-1" # "Tcp"
  type              = "egress"
  security_group_id = aws_security_group.securitygroup.id
}

data "aws_key_pair" "public_key" {
  key_name           = "public_key"
  include_public_key = false
}

resource "aws_instance" "ec2instance" {
  instance_type           = "t2.micro"
  ami                     = "ami-0c7217cdde317cfec" # from other script
  subnet_id               = aws_subnet.instance.id
  vpc_security_group_ids  = [aws_security_group.securitygroup.id] # hashicorp/terraform-provider-aws#23693
  key_name                = data.aws_key_pair.public_key.key_name
  disable_api_termination = false
  ebs_optimized           = false
  root_block_device {
    volume_size = "10"
  }
  tags = {
    "Name" = "DummyMachine"
  }
}

output "instance_private_ip" {
  value = aws_instance.ec2instance.private_ip
}

//

resource "aws_subnet" "nat_gateway" {
  availability_zone = data.aws_availability_zones.available.names[0]
  cidr_block        = "10.0.2.0/24" // [ '10.0.2.0', '10.0.2.255' ]
  vpc_id            = aws_vpc.vpc.id
  tags              = {
    "Name" = "DummySubnetNAT"
  }
}

resource "aws_internet_gateway" "nat_gateway" {
  vpc_id = aws_vpc.vpc.id
  tags   = {
    "Name" = "DummyGateway"
  }
}

resource "aws_route_table" "nat_gateway" {
  vpc_id = aws_vpc.vpc.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.nat_gateway.id
  }
}

resource "aws_route_table_association" "nat_gateway" {
  subnet_id      = aws_subnet.nat_gateway.id
  route_table_id = aws_route_table.nat_gateway.id
}

//

resource "aws_eip" "nat_gateway" {
  domain = "vpc"
  tags   = { Name : "34th" }
}

resource "aws_nat_gateway" "nat_gateway" {
  allocation_id = aws_eip.nat_gateway.id
  subnet_id     = aws_subnet.nat_gateway.id
  tags          = {
    "Name" = "DummyNatGateway"
  }
}

output "nat_gateway_ip" {
  value = aws_eip.nat_gateway.public_ip
}

resource "aws_route_table" "instance" {
  vpc_id = aws_vpc.vpc.id
  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.nat_gateway.id
  }
}

resource "aws_route_table_association" "instance" {
  subnet_id      = aws_subnet.instance.id
  route_table_id = aws_route_table.instance.id
}

//

variable "enable_jump_host" { default = false }

resource "aws_instance" "ec2jumphost" {
  count                   = var.enable_jump_host ? 1 : 0
  instance_type           = "t2.micro"
  ami                     = "ami-0c7217cdde317cfec"
  subnet_id               = aws_subnet.nat_gateway.id
  vpc_security_group_ids  = [aws_security_group.securitygroup.id] # hashicorp/terraform-provider-aws#23693
  key_name                = data.aws_key_pair.public_key.key_name
  disable_api_termination = false
  ebs_optimized           = false
  root_block_device {
    volume_size = "10"
  }
  tags = {
    "Name" = "DummyMachineJumphost"
  }
}

# aws ec2 describe-addresses --query 'join(`\n`, Addresses[*].join(`: `, [PublicIp,AllocationId]))' --output text
resource "aws_eip" "jumphost" {
  count    = var.enable_jump_host ? 1 : 0
  instance = aws_instance.ec2jumphost[0].id
  domain   = "vpc"
}

output "jumphost_ip" {
  value = var.enable_jump_host ? aws_eip.jumphost[0].public_ip : null
}
