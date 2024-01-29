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
  availability_zone = each.key

  vpc_id     = aws_vpc.fg_vpc.id
  cidr_block = each.value["cidr"]
  tags       = {
    Name = "fg-${each.key}"
  }
}

resource "aws_internet_gateway" "fg_vpc_igw" {
  vpc_id = aws_vpc.fg_vpc.id
  tags   = {
    Name = "fg-vpc-igw"
  }
}

# https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Route_Tables.html
resource "aws_route_table" "fg_vpc_rt" {
  vpc_id = aws_vpc.fg_vpc.id
  tags   = { Name : "fg-vpc-rt" }
}

resource "aws_route_table_association" "fg_vpc_rt_as_local" {
  for_each = aws_subnet.fg

  route_table_id = aws_route_table.fg_vpc_rt.id
  subnet_id      = each.value.id
}

resource "aws_route" "fg_vpc_rt_route_igw" {
  route_table_id         = aws_route_table.fg_vpc_rt.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.fg_vpc_igw.id
}

# use import!?!?
# no.
# https://docs.aws.amazon.com/vpc/latest/userguide/VPC_Route_Tables.html
#
# "Every route table contains a local route for communication within the VPC. This route is
# added by default to all route tables. If your VPC has more than one IPv4 CIDR block, your
# route tables contain a local route for each IPv4 CIDR block. [...] You cannot modify or
# delete these routes in a subnet route table or in the main route table."
#
#resource "aws_route" "fg_vpc_rt_route_local" {
#  route_table_id         = aws_route_table.fg_vpc_rt.id
#  destination_cidr_block = aws_vpc.fg_vpc.cidr_block
#  gateway_id             = "local"
#}

# but that is still not enough to pull from registry.

resource "aws_security_group" "allow_all" {
  name = "sec-allow-all"
  tags = { Name : "sec-allow-all" }

  vpc_id = aws_vpc.fg_vpc.id
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

resource "aws_ecs_cluster" "fargate_cluster" {
  name = "fargate"
  tags = { Name : "fargate" }
}

resource "aws_ecs_task_definition" "fargate_apache_task" {
  family = "fargate-apache-task"
  cpu    = 256
  memory = "512"

  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]

  container_definitions = jsonencode([
    {
      name : "fargate-app",
      image : "public.ecr.aws/docker/library/httpd:latest",
      portMappings : [
        {
          "containerPort" : 80,
          "hostPort" : 80,
          "protocol" : "tcp"
        }
      ],
      essential : true,
      entryPoint : [
        "sh",
        "-c"
      ],
      command : [
        "/bin/sh -c \"echo '<html> <head> <title>Amazon ECS Sample App</title> <style>body {margin-top: 40px; background-color: #333;} </style> </head><body> <div style=color:white;text-align:center> <h1>Amazon ECS Sample App</h1> <h2>Congratulations!</h2> <p>Your application is now running on a container in Amazon ECS.</p> </div></body></html>' >  /usr/local/apache2/htdocs/index.html && httpd-foreground\""
      ]
    }
  ])
}

# disabling while we figure out the route to public internet situation
/*
resource "aws_ecs_service" "fargate_apache_service" {
  name            = "fargate-apache-service"
  cluster         = aws_ecs_cluster.fargate_cluster.id
  task_definition = aws_ecs_task_definition.fargate_apache_task.arn
  desired_count   = 1
  network_configuration {
    subnets         = [for a in aws_subnet.fg : a.id]
    security_groups = [aws_security_group.allow_all.id]
  }
  capacity_provider_strategy {
    capacity_provider = "FARGATE" # consider learning the asg based one also
    weight            = 1
  }
  load_balancer {
    target_group_arn = aws_alb_target_group.fargate_tg.arn
    container_name   = jsondecode(aws_ecs_task_definition.fargate_apache_task.container_definitions)[0]["name"]
    container_port   = 80
  }
}
*/

# disabling to save money
/*
resource "aws_lb" "fargate_lb" {
  name               = "fargate-apache-lb"
  internal           = false
  load_balancer_type = "application"
  # subnet must be in vpc having igw
  subnets            = [for a in aws_subnet.fg : a.id]
  security_groups    = [aws_security_group.allow_all.id]
  tags               = {
    Name = "fargate-apache-lb"
  }
}

resource "aws_alb_target_group" "fargate_tg" {
  name        = "fargate-apache-tg"
  port        = 80
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = aws_vpc.fg_vpc.id
}

resource "aws_lb_listener" "fargate_lb_listener" {
  load_balancer_arn = aws_lb.fargate_lb.arn
  port              = 80
  protocol          = "HTTP"
  default_action {
    type             = "forward"
    target_group_arn = aws_alb_target_group.fargate_tg.arn
  }
}
*/

# this seems to only be for individual vms
#resource "aws_lb_target_group_attachment" "fargate_lb_tg" {
#  target_group_arn = aws_alb_target_group.fargate_tg.arn
#  target_id        = aws_lb.fargate_lb.id
#}
