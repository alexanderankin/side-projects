# terraform-provider-ssh-connection

A Terraform/OpenTofu provider exposing the `sshconnection_connection` ephemeral
resource. It runs the system OpenSSH client for connections and port forwards
that last while an operation needs them.

See the [provider documentation](docs/index.md),
[resource reference](docs/ephemeral-resources/connection.md), and
[internal walkthrough](internal/how_it_works.md). The walkthrough explains the
code for readers who know Terraform and SSH but are new to Go. The original
cleanup plan is saved in [plan.md](plan.md).

Requires Go 1.27 or newer and OpenSSH on `PATH`.

## Configuration and readiness

SSH always runs with `-F none`, ignoring both user and system SSH configuration.
Move host aliases, users, ports, custom identities, and forwards into Terraform.
`config_file` is deprecated and accepts only `none`. Normal SSH agent use,
default identity-file discovery, and known-hosts checks still apply.

Opening waits up to 60 seconds for authentication, local listeners, and server
acknowledgement of remote forwards. Readiness does not check destination services.
Consumers must depend on the ephemeral connection to keep it open during use.
Cancellation, timeout, and startup failure close the connection before returning.

Conflicting custom options are rejected. The provider requires debug logging for
readiness, failed-forward detection, foreground execution, independent connections,
and noninteractive authentication. See the walkthrough for the required-option
table. Routine output is always captured internally; `quiet` remains accepted but
has no additional effect. `log_file` receives a provider-written copy of debug logs.

## Tests

```sh
go test -race ./...
go vet ./...
```

Tests exercise the real OpenSSH client against an isolated local SSH server,
including usable forwarding, delayed/rejected remote acknowledgement, occupied
ports, cancellation, and cleanup. Provider lifecycle tests use the actual framework
RPC boundary with fake connections. No external host or credentials are needed.

The neighboring `terraform-provider-sshconnection-test` is a separate live test
requiring access to its configured host and service.

## Generated documentation

HashiCorp's `tfplugindocs` generates `docs/` from the live schema and the Terraform
files under `examples/`. Regenerate from the provider root:

```sh
go generate ./...
```

Update schema descriptions, `internal/provider/providerDoc.md`, or examples before
regenerating. Do not edit generated `docs/` pages directly. The internal walkthrough
and this README are maintained by hand.
