terraform {
  required_version = ">= 1.10.0"

  required_providers {
    ssh = {
      source  = "tfrepo.internal.company.com/internal/ssh"
      version = "0.1.0"
    }
  }
}
