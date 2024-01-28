terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.34.0"
    }
  }
}

provider "aws" {
  # Configuration options
  region = "us-east-1"
}

#https://www.andreagrandi.it/2017/08/25/getting-latest-ubuntu-ami-with-terraform/

data "aws_ami" "ubuntu" {
  most_recent = true

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-arm64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  #owners = ["099720109477"] # Canonical
  # https://stackoverflow.com/a/76841570
  owners = ["amazon"] # Canonical
}

output "aws_ami" {
  value = data.aws_ami.ubuntu
}


