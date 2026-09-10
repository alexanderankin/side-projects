package provider

import (
	"context"
	_ "embed"

	"github.com/hashicorp/terraform-plugin-framework-validators/listvalidator"
	"github.com/hashicorp/terraform-plugin-framework-validators/stringvalidator"
	"github.com/hashicorp/terraform-plugin-framework/ephemeral"
	"github.com/hashicorp/terraform-plugin-framework/ephemeral/schema"
	"github.com/hashicorp/terraform-plugin-framework/schema/validator"
	"github.com/hashicorp/terraform-plugin-framework/types"
)

//go:embed ssh_connectionDoc.md
var sshConnectionDoc string

func (resource *sshConnectionResource) Schema(_ context.Context, _ ephemeral.SchemaRequest, response *ephemeral.SchemaResponse) {
	forwardAttributes := map[string]schema.Attribute{
		"bind_address": schema.StringAttribute{
			Optional:            true,
			MarkdownDescription: "Address on which SSH listens. Omit it to use the SSH default.",
		},
		"port": schema.StringAttribute{
			Required:            true,
			MarkdownDescription: "Listening TCP port.",
		},
		"host": schema.StringAttribute{
			Required:            true,
			MarkdownDescription: "Destination host reached through the tunnel.",
		},
		"host_port": schema.StringAttribute{
			Required:            true,
			MarkdownDescription: "Destination TCP port.",
		},
	}

	response.Schema = schema.Schema{
		MarkdownDescription: sshConnectionDoc,
		Attributes: map[string]schema.Attribute{
			"destination": schema.StringAttribute{
				Required:            true,
				MarkdownDescription: "SSH destination, such as `user@example.com`. This is the first positional argument.",
			},
			"identity_file": schema.StringAttribute{
				Optional:            true,
				MarkdownDescription: "Identity file (`-i`).",
			},
			"port": schema.Int64Attribute{
				Optional:            true,
				MarkdownDescription: "SSH server port (`-p`).",
			},
			"login_name": schema.StringAttribute{
				Optional:            true,
				MarkdownDescription: "Remote login name (`-l`).",
			},
			"ip_version": schema.StringAttribute{
				Optional:            true,
				MarkdownDescription: "IP family: `default` (the default), `ipv4`, or `ipv6`.",
				Validators:          []validator.String{stringvalidator.OneOf("default", "ipv4", "ipv6")},
			},
			"agent_connection_forwarding": schema.BoolAttribute{
				Optional:            true,
				MarkdownDescription: "Enable (`-A`) or explicitly disable (`-a`) authentication-agent forwarding. Defaults to false.",
			},
			"cipher_spec": schema.ListAttribute{
				Optional:            true,
				ElementType:         types.StringType,
				MarkdownDescription: "Ordered cipher list, passed to `-c` as a comma-separated value.",
			},
			"log_file": schema.StringAttribute{
				Optional:            true,
				MarkdownDescription: "Append a copy of internal SSH debug logs to this file. The provider writes the file; it does not pass -E to SSH.",
			},
			"config_file": schema.StringAttribute{
				Optional:            true,
				DeprecationMessage:  "External SSH configuration is disabled. Move settings into Terraform and remove config_file.",
				MarkdownDescription: "Deprecated. Only `none` is accepted; SSH always runs with `-F none` and ignores user and system configuration files.",
			},
			"mac_spec": schema.ListAttribute{
				Optional:            true,
				ElementType:         types.StringType,
				MarkdownDescription: "Ordered MAC list, passed to `-m` as a comma-separated value.",
			},
			"quiet": schema.BoolAttribute{
				Optional: true,
				MarkdownDescription: "Compatibility setting. " +
					"Routine SSH output is always captured internally, regardless of this value. " +
					"Readiness diagnostics, failure details, and log_file copies remain available.",
			},
			"pty_allocation": schema.BoolAttribute{
				Optional:            true,
				MarkdownDescription: "Force (`-t`) or disable (`-T`) pseudo-terminal allocation. Defaults to false.",
			},
		},
		Blocks: map[string]schema.Block{
			"jump_host": schema.ListNestedBlock{
				MarkdownDescription: "Optional jump host (`-J`).",
				Validators:          []validator.List{listvalidator.SizeAtMost(1)},
				NestedObject: schema.NestedBlockObject{
					Attributes: map[string]schema.Attribute{
						"jump_host_destination": schema.StringAttribute{
							Required:            true,
							MarkdownDescription: "SSH jump destination.",
						},
					},
				},
			},
			"listen": schema.ListNestedBlock{
				MarkdownDescription: "Repeatable local TCP forwarding (`-L`).",
				NestedObject:        schema.NestedBlockObject{Attributes: forwardAttributes},
			},
			"remote_listen": schema.ListNestedBlock{
				MarkdownDescription: "Repeatable remote TCP forwarding (`-R`).",
				NestedObject:        schema.NestedBlockObject{Attributes: forwardAttributes},
			},
			"ssh_option": schema.ListNestedBlock{
				MarkdownDescription: "Repeatable option (`-o Name=Value`). " +
					"Conflicts with required settings are rejected: LogLevel=DEBUG1, ExitOnForwardFailure=yes, ForkAfterAuthentication=no, ControlMaster=no, ControlPath=none, ControlPersist=no, BatchMode=yes, ClearAllForwardings=no, SessionType=none. " +
					"Include, Host, and Match are unsupported.",
				NestedObject: schema.NestedBlockObject{
					Attributes: map[string]schema.Attribute{
						"name": schema.StringAttribute{
							Required:            true,
							MarkdownDescription: "see `man ssh_config`",
						},
						"value": schema.StringAttribute{
							Required:            true,
							MarkdownDescription: "see `man ssh_config`",
						},
					},
				},
			},
		},
	}
}
