terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.91.0"
    }
    local = {
      source  = "hashicorp/local"
      version = "2.5.3"
    }
  }
}

provider "aws" {
  region = "us-east-1"
}

variable "vpc_name" {}
variable "subnet_name" {}
variable "sg_name" {}
variable "jump_host_name" {}
variable "variables_file" {}

data "aws_vpcs" "vpcs" {
  filter {
    name   = "tag:Name"
    values = [var.vpc_name]
  }
}

data "aws_subnets" "subnets" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpcs.vpcs.ids[0]]
  }

  filter {
    name   = "tag:Name"
    values = [var.subnet_name]
  }
}

data "aws_security_groups" "sg" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpcs.vpcs.ids[0]]
  }

  filter {
    name   = "tag:Name"
    values = [var.sg_name]
  }
}

data "aws_instances" "jump_host" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpcs.vpcs.ids[0]]
  }
  filter {
    name   = "tag:Name"
    values = [var.jump_host_name]
  }
}

data "aws_instance" "jump_host" {
  instance_id = data.aws_instances.jump_host.ids[0]
}

locals {
  packer_vars = {
    vpc_id               = data.aws_vpcs.vpcs.ids[0]
    subnet_id            = data.aws_subnets.subnets.ids[0]
    sg_id                = data.aws_security_groups.sg.ids[0]
    jump_host_ip_address = data.aws_instance.jump_host.public_ip
    jump_host_user       = "ubuntu"
  }
}

output "debug" {
  value = local.packer_vars
}

resource "local_file" "packer_vars" {
  filename = "${path.module}/build/${var.variables_file}"

  content = join("\n", [
    for k, v in local.packer_vars :
    "${k} = ${jsonencode(v)}"
  ])
}
