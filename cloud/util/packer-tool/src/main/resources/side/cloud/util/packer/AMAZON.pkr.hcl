packer {
  required_plugins {
    amazon = {
      version = ">= 1.2.8"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

variable "vpc_id" {}
variable "subnet_id" {}
variable "sg_id" {}
variable "jump_host_ip_address" {}
variable "jump_host_user" {}

locals {
  ssh_private_key_file = "build/id_rsa"
  ssh_public_key       = file("build/id_rsa.pub")
}

source "amazon-ebs" "ubuntu" {
}

locals { t = replace(replace(replace(timestamp(), ":", "-"), "T", "-"), "Z", "") }

build {
  name = "PROJECT_NAME"
  dynamic "source" {
    labels = ["amazon-ebs.ubuntu"]
    for_each = {
      amd64 = {
        instance_type = "t3.small"
        arch          = "amd64"
      }
      arm64 = {
        instance_type = "t4g.small"
        arch          = "arm64"
      }
    }
    content {
      ami_name = "PROJECT_NAME-${source.key}-${local.t}"
      tags = { Name = "PROJECT_NAME-${source.key}-${local.t}" }
      source_ami_filter {
        filters = {
          name                = "ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-${source.value.arch}-server-*"
          root-device-type    = "ebs"
          virtualization-type = "hvm"
        }
        most_recent = true
        owners      = ["amazon"]
      }

      region        = "us-east-1"
      instance_type = source.value.instance_type
      ssh_username  = "ubuntu"
      # iam_instance_profile =
      security_group_ids           = [var.sg_id]
      subnet_id                    = var.subnet_id
      vpc_id                       = var.vpc_id
      user_data                    = <<-EOF
        #!/bin/bash
        f=/home/ubuntu/.ssh/authorized_keys; touch $f; chmod 600 $f;
        echo "${local.ssh_public_key}" >> $f
      EOF
      ssh_interface                = "private_ip"
      ssh_bastion_host             = var.jump_host_ip_address
      ssh_bastion_username         = var.jump_host_user
      ssh_bastion_private_key_file = local.ssh_private_key_file
    }
  }

  provisioner "shell" {
    inline = ["#!/usr/bin/env bash", "set -eux -o pipefail; cloud-init status --wait"]
  }

  # provisioner "shell" {
  #   script = "setup.sh"
  # }

  provisioner "shell" {
    inline = ["#!/usr/bin/env bash", "set -eux -o pipefail; cat /dev/null > /home/ubuntu/.ssh/authorized_keys"]
  }

  post-processor "manifest" {
    output = "build/manifest.json"
  }
}
