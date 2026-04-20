provider "aws" {
  region = "us-east-1"
}

locals { env = "example" }
locals { domain = "mycompany.com" }

module "app" {
  source = "../../module"

  environment = local.env

  site_domain_name         = "${local.env}.${local.domain}"
  site_hosted_zone_id      = null
  frontend_certificate_arn = null

  api_domain_name         = "api.${local.env}.${local.domain}"
  api_hosted_zone_id      = null
  api_certificate_arn     = null

  lambda_zip_path = null
  lambda_handler            = "side.learning.springbootlambda.LearningSpringBootLambdaHandler::handleRequest"

  subnet_ids         = []
  security_group_ids = []

  lambda_environment = {
    SPRING_MAIN_BANNER_MODE = "off"
  }
}
