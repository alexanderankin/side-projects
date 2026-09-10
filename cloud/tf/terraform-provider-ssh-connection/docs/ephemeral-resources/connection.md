---
page_title: "ssh_connection Ephemeral Resource - SSH"
description: |-
  Opens an operation-scoped SSH connection, including local and remote port forwarding.
---

# ssh_connection (Ephemeral Resource)

Starts `ssh -N -o ExitOnForwardFailure=yes` and keeps it alive for as long as Terraform or OpenTofu needs the ephemeral value. Close terminates and reaps the process. SSH configuration and authentication behave exactly as they do for the installed OpenSSH client.

## Example Usage

```terraform
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
```

## Argument Reference

- `destination` (String, Required) — The sole positional SSH destination, for example `user@example.com`.
- `ip_version` (String, Optional) — `default`, `ipv4` (`-4`), or `ipv6` (`-6`). Defaults to `default`.
- `agent_connection_forwarding` (Boolean, Optional) — Uses `-A` when true and `-a` when false. Defaults to false.
- `cipher_spec` (List of String, Optional) — Ordered values joined with commas for `-c`.
- `log_file` (String, Optional) — Debug log file passed with `-E`.
- `config_file` (String, Optional) — Alternative client config passed with `-F`.
- `identity_file` (String, Optional) — Identity passed with `-i`.
- `login_name` (String, Optional) — Login name passed with `-l`.
- `mac_spec` (List of String, Optional) — Ordered values joined with commas for `-m`.
- `port` (Number, Optional) — SSH server port passed with `-p`.
- `quiet` (Boolean, Optional) — Adds `-q` when true. Defaults to false.
- `pty_allocation` (Boolean, Optional) — Uses `-t` when true and `-T` when false. Defaults to false.

### `jump_host` block

At most one block is accepted. `jump_host_destination` is required and is passed with `-J`.

### `listen` and `remote_listen` blocks

Both blocks are repeatable. `listen` produces `-L`; `remote_listen` produces `-R`.

- `bind_address` (String, Optional) — Listen address. An explicitly empty string preserves OpenSSH's leading-colon form, while omission leaves the address out.
- `port` (String, Required) — Listening port.
- `host` (String, Required) — Forward destination host.
- `host_port` (String, Required) — Forward destination port.

Only TCP forwarding forms are modeled. Unix sockets and dynamic remote forwarding are outside this resource's scope.
