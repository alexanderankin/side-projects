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
  # Configuration options
  region = "us-east-1"
}

#provider "tls" {
#}
/*

resource "aws_vpc" "vpc" {
  cidr_block = "10.0.0.0/26" // [ '10.0.0.0', '10.0.0.63' ]
  tags       = {
    Name = "vpc-static"
  }
}

resource "aws_subnet" "subnet1" {
  vpc_id            = aws_vpc.vpc.id
  availability_zone = "us-east-1a"
  cidr_block        = "10.0.0.0/28" // [ '10.0.0.0', '10.0.0.15' ]
  tags              = {
    Name = "static-subnet1"
  }
}

resource "aws_subnet" "subnet2" {
  vpc_id            = aws_vpc.vpc.id
  availability_zone = "us-east-1b"
  cidr_block        = "10.0.0.16/28" // [ '10.0.0.16', '10.0.0.31' ]
  tags              = {
    Name = "static-subnet2"
  }
}

resource "aws_subnet" "subnet_public" {
  vpc_id            = aws_vpc.vpc.id
  availability_zone = "us-east-1b"
  cidr_block        = "10.0.0.32/28" // [ '10.0.0.32', '10.0.0.47' ]
  tags              = {
    Name = "static-subnet-public"
  }
  map_customer_owned_ip_on_launch = true
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/instance
data "aws_ami" "ubuntu" {
  most_recent = true

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  owners = ["099720109477"] # Canonical
}

resource "aws_instance" "web" {
  for_each = {
    "private": false
    "public": true
  }

  ami           = data.aws_ami.ubuntu.id
  instance_type = "t3.micro"

  tags = {
    Name = "HelloWorld-${each.value}"
  }

  subnet_id = each.value ? aws_subnet.subnet_public.id : aws_subnet.subnet1.id
  vpc_security_group_ids = []
}
*/


/*
resource "aws_eip" "my_static_ip" {
  instance = null
  domain   = "vpc"
  tags     = {
    Name = "ip-static"
  }
}
*/

/*
resource "aws_key_pair" "key_pair" {
  key_name   = "public_key"
  public_key = ""
}
*/

/*
resource "aws_instance" "my_ec2_instance" {
  ami                         = data.aws_ami.ubuntu.id
  instance_type               = "t3.micro"
  availability_zone           = "us-east-1a"
  subnet_id                   = aws_subnet.subnet1.id
  associate_public_ip_address = true
  key_name                    = aws_key_pair.key_pair.key_name
  tags                        = {
    Name = "ec2-static"
  }
  iam_instance_profile = aws_iam_instance_profile.iam_profile.id
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.vpc.id
  tags   = {
    Name = "igw-static"
  }
}

#resource "aws_nat_gateway" "my_nat_gateway" {
#  allocation_id = aws_eip.my_static_ip.id
#  subnet_id     = aws_subnet.subnet1.id
#}

resource "aws_route_table" "my_route_table" {
  vpc_id = aws_vpc.vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }
  tags = { Name : "table-for-static" }
}

resource "aws_route_table_association" "table_to_subnet1" {
  subnet_id      = aws_subnet.subnet1.id
  route_table_id = aws_route_table.my_route_table.id
}

resource "aws_route_table_association" "table_to_subnet2" {
  subnet_id      = aws_subnet.subnet2.id
  route_table_id = aws_route_table.my_route_table.id
}


# https://github.com/bayupw/terraform-aws-ssm-instance-profile/blob/main/main.tf
# https://us-east-1.console.aws.amazon.com/iam/home?region=us-east-1#/roles/create?selectedUseCase=SimpleSystemsManagerServiceRole&trustedEntityType=AWS_SERVICE&selectedService=EC2&policies=arn%3Aaws%3Aiam%3A%3Aaws%3Apolicy%2FAmazonSSMManagedInstanceCore
# this is called the trust policy
*/
resource "aws_iam_role" "iam_role" {
  name               = "ssm-role"
  # language=json
  assume_role_policy = <<-EOF
    {
      "Version": "2012-10-17",
      "Statement": {
        "Effect": "Allow",
        "Principal": { "Service": "ec2.amazonaws.com" },
        "Action": "sts:AssumeRole"
      }
    }
  EOF
}

resource "aws_iam_instance_profile" "iam_profile" {
  name = "ssm_profile"
  role = aws_iam_role.iam_role.name
}

resource "aws_iam_role_policy_attachment" "this" {
  role       = aws_iam_role.iam_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}
