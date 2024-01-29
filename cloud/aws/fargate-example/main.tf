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

/*


│ Error: registering ELBv2 Target Group (arn:aws:elasticloadbalancing:us-east-1:662610805887:targetgroup/fargate-apache-tg/4aed2f0f339a28ab) target: ValidationError: Instance ID 'arn:aws:elasticloadbalancing:us-east-1:662610805887:loadbalancer/app/fargate-apache-lb/b41a63a0714407d6' is not valid
│ Error: registering ELBv2 Target Group () target: ValidationError: Instance ID '' is not valid
│       status code: 400, request id: e1a475b0-e7fd-43a6-a544-587c9ef18f42

*/
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

# this seems to only be for individual vms
#resource "aws_lb_target_group_attachment" "fargate_lb_tg" {
#  target_group_arn = aws_alb_target_group.fargate_tg.arn
#  target_id        = aws_lb.fargate_lb.id
#}
