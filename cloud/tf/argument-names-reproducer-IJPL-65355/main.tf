variable "network_module" {
  default = {
    subnets = {
      example0 = { id : "0" }
      example1 = { id : "1" }
      example2 = { id : "2" }
    }
  }
}

locals {
  subnets = [for i in range(3) : "example${i}"]
}

output "test" {
  value = merge(flatten([
    for each_sg_name in local.subnets : [
      for each_port in [
        {
          type = "ingress"
          port = 443
        },
        {
          type = "ingress"
          port = 22
        },
        {
          type = "egress"
          port = 443
        },
      ] :
      {
        "${each_sg_name}-${each_port.type}-${each_port.port}" = {
          id   = var.network_module["subnets"][each_sg_name]["id"]
          type = each_port["type"]
          port = each_port["port"]
        }
      }
    ]
  ])...)
}
