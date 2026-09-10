# terraform-provider-ssh-connection

A deliberately small Terraform/OpenTofu provider exposing the `ssh_connection` ephemeral resource. It runs the system OpenSSH client for operation-scoped connections and port forwards.

See [the provider documentation](docs/index.md) and [resource reference](docs/ephemeral-resources/connection.md).

Requires Go 1.25 or newer. Run `go test ./...` to build and test it.
