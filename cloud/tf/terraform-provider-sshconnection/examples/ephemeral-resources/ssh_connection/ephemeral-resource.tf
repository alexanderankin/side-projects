ephemeral "ssh_connection" "database" {
  destination   = "tunnel@example.com"
  identity_file = pathexpand("~/.ssh/id_ed25519")
  ip_version    = "ipv4"

  jump_host {
    jump_host_destination = "bastion@example.com"
  }

  listen {
    bind_address = "127.0.0.1"
    port         = "15432"
    host         = "database.internal"
    host_port    = "5432"
  }
}
