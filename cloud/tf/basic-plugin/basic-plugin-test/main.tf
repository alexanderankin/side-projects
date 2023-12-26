terraform {
  required_providers {
    basic = {
      source = "basic-plugin"
    }
  }
}

provider "basic" {}

output "test" {
  value = "hello, world"
}

data "basic_example" "e" {}

output "e" {
  value = data.basic_example.e
}
