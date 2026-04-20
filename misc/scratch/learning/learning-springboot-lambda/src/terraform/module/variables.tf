variable "environment" {}
variable "site_domain_name" {}
variable "site_hosted_zone_id" {}
variable "frontend_certificate_arn" {}

variable "api_domain_name" {}
variable "api_hosted_zone_id" {}
variable "api_certificate_arn" {}

variable "lambda_zip_path" {}
variable "lambda_handler" {}

variable "subnet_ids" {
  type = list(string)
}

variable "security_group_ids" {
  type = list(string)
}

variable "lambda_environment" {
  type    = map(string)
  default = {}
}
