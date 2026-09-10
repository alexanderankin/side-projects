terraform {
  required_providers {
    sshconnection = {
      source = "terraform.example.com/side-projects/sshconnection"
    }
  }
}

provider "sshconnection" {}
