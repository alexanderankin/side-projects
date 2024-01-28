data "aws_availability_zones" "zones" {}

resource "aws_vpc" "fg_vpc" {
  cidr_block = "10.0.0.0/24"
  tags       = {
    Name = "fg"
  }
}

resource "aws_subnet" "fg" {
  for_each = {
    "us-east-1a" : { cidr : "10.0.0.0/27" }
    #"us-east-1b" : { cidr : "10.0.0.64/26"}
    "us-east-1c" : { cidr : "10.0.0.128/27" }
    #"us-east-1d" : { cidr : "10.0.0.192/26"}
  }
  vpc_id     = aws_vpc.fg_vpc.id
  cidr_block = each.value["cidr"]
  tags       = {
    Name = "fg-${each.key}"
  }
}

resource "aws_security_group" "allow_all" {
  name = "sec-allow-all"
  tags = { Name : "sec-allow-all" }
}

resource "aws_vpc_security_group_egress_rule" "allow" {
  security_group_id = aws_security_group.allow_all.id
  cidr_ipv4         = "10.0.0.0/24"
  ip_protocol       = "-1" # both
  description       = "allow everything"
  tags              = { Name : "allow-egress" }
}

resource "aws_vpc_security_group_ingress_rule" "allow" {
  security_group_id = aws_security_group.allow_all.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1" # both
  description       = "allow everything"
  tags              = { Name : "allow-ingress" }
}
