# SSH connection example

This configuration demonstrates the sibling `terraform-provider-sshconnection` provider by opening a loopback-only PostgreSQL tunnel for the duration of an apply.

For local development, build the provider and configure a Terraform/OpenTofu CLI development override:

```sh
mkdir -p ~/.terraform.d/plugins/local
go build -o ~/.terraform.d/plugins/local/terraform-provider-ssh ../terraform-provider-ssh-connection
```

Add this to your CLI configuration (`~/.terraformrc` for Terraform or `~/.tofurc` for OpenTofu):

```hcl
provider_installation {
  dev_overrides {
    "tfrepo.internal.company.com/internal/ssh" = "/Users/you/.terraform.d/plugins/local"
  }
  direct {}
}
```

Copy `terraform.tfvars.example` to `terraform.tfvars`, set the destination and database host, then run `terraform apply` or `tofu apply`. The installed `ssh` executable handles normal config, known-host, agent, and authentication behavior.

The ephemeral block is intentionally the entire example: it has no persisted outputs and the provider terminates SSH when the operation releases it.
