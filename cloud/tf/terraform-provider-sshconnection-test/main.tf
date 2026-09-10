terraform {
  required_providers {
    sshconnection = {
      source  = "terraform.example.com/side-projects/sshconnection"
      version = "1.0.0"
    }
    local = {
      source  = "hashicorp/local"
      version = "2.7.0"
    }
  }
}

ephemeral "sshconnection_connection" "connection" {
  destination = "ankin.info"
  listen {
    bind_address = "127.0.0.1"
    port         = "18080"
    host         = "127.0.0.1"
    host_port    = "80"
  }
}

resource "terraform_data" "sleep" {
  input            = { now = timestamp() }
  triggers_replace = { now = timestamp() }

  provisioner "local-exec" {
    interpreter = ["bash", "-c"]
    command     = <<-EOF
      true
    EOF
  }
}

data "local_command" "output" {
  command = "bash"
  arguments = [
    "-c",
    <<-EOF
      curl -v localhost:18080
    EOF
  ]

  allow_non_zero_exit_code = true

  depends_on = [terraform_data.sleep]
}

output "output" {
  value = data.local_command.output
}
