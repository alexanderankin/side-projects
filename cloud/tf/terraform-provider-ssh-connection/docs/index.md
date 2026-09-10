---
page_title: "Provider: SSH"
description: |-
  Opens SSH connections for the duration of a Terraform or OpenTofu operation.
---

# SSH Provider

The SSH provider supplies one ephemeral resource, `ssh_connection`. It shells out to the `ssh` executable on `PATH`; it does not implement SSH itself. The connection is started when the ephemeral resource opens and stopped when the operation no longer needs it.

This provider has no configuration arguments.

```terraform
terraform {
  required_providers {
    ssh = {
      source = "registry.terraform.io/toor/ssh-connection"
    }
  }
}
```
