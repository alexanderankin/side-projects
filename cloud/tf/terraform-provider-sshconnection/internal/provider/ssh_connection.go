package provider

import (
	"context"
	"crypto/rand"
	"encoding/hex"
	"fmt"
	"strconv"
	"sync"
	"time"

	"github.com/toor/terraform-provider-ssh-connection/internal/connection"

	"github.com/hashicorp/terraform-plugin-framework-validators/listvalidator"
	"github.com/hashicorp/terraform-plugin-framework-validators/stringvalidator"
	"github.com/hashicorp/terraform-plugin-framework/ephemeral"
	"github.com/hashicorp/terraform-plugin-framework/ephemeral/schema"
	"github.com/hashicorp/terraform-plugin-framework/schema/validator"
	"github.com/hashicorp/terraform-plugin-framework/types"
)

const processKey = "ssh_connection_provider_process_id"

var _ ephemeral.EphemeralResource = (*sshConnectionResource)(nil)
var _ ephemeral.EphemeralResourceWithClose = (*sshConnectionResource)(nil)

type sshConnectionResource struct {
	mu        sync.Mutex
	processes map[string]connection.Connection
	start     func(context.Context, connection.Model) (connection.Connection, error)
}

type sshConnectionModel = connection.Model

func NewSSHConnection() ephemeral.EphemeralResource {
	return &sshConnectionResource{processes: make(map[string]connection.Connection), start: connection.StartCLI}
}

func (r *sshConnectionResource) Metadata(_ context.Context, req ephemeral.MetadataRequest, resp *ephemeral.MetadataResponse) {
	resp.TypeName = req.ProviderTypeName + "_connection"
}

func (r *sshConnectionResource) Schema(_ context.Context, _ ephemeral.SchemaRequest, resp *ephemeral.SchemaResponse) {
	forwardAttributes := map[string]schema.Attribute{
		"bind_address": schema.StringAttribute{Optional: true, MarkdownDescription: "Address on which SSH listens. Omit it to use the SSH default."},
		"port":         schema.StringAttribute{Required: true, MarkdownDescription: "Listening TCP port."},
		"host":         schema.StringAttribute{Required: true, MarkdownDescription: "Destination host reached through the tunnel."},
		"host_port":    schema.StringAttribute{Required: true, MarkdownDescription: "Destination TCP port."},
	}

	resp.Schema = schema.Schema{
		MarkdownDescription: "Starts `ssh -N` and waits up to 60 seconds for authentication, local listeners, and server acknowledgement of remote forwards. Stops SSH on failure, cancellation, or close. Readiness does not check the services behind the forwards. SSH must be available on `PATH`.",
		Attributes: map[string]schema.Attribute{
			"destination":                 schema.StringAttribute{Required: true, MarkdownDescription: "SSH destination, such as `user@example.com`. This is the first positional argument."},
			"identity_file":               schema.StringAttribute{Optional: true, MarkdownDescription: "Identity file (`-i`)."},
			"port":                        schema.Int64Attribute{Optional: true, MarkdownDescription: "SSH server port (`-p`)."},
			"login_name":                  schema.StringAttribute{Optional: true, MarkdownDescription: "Remote login name (`-l`)."},
			"ip_version":                  schema.StringAttribute{Optional: true, MarkdownDescription: "IP family: `default` (the default), `ipv4`, or `ipv6`.", Validators: []validator.String{stringvalidator.OneOf("default", "ipv4", "ipv6")}},
			"agent_connection_forwarding": schema.BoolAttribute{Optional: true, MarkdownDescription: "Enable (`-A`) or explicitly disable (`-a`) authentication-agent forwarding. Defaults to false."},
			"cipher_spec":                 schema.ListAttribute{Optional: true, ElementType: types.StringType, MarkdownDescription: "Ordered cipher list, passed to `-c` as a comma-separated value."},
			"log_file":                    schema.StringAttribute{Optional: true, MarkdownDescription: "File to receive SSH debug logs (`-E`)."},
			"config_file":                 schema.StringAttribute{Optional: true, MarkdownDescription: "Alternative SSH client configuration file (`-F`)."},
			"mac_spec":                    schema.ListAttribute{Optional: true, ElementType: types.StringType, MarkdownDescription: "Ordered MAC list, passed to `-m` as a comma-separated value."},
			"quiet":                       schema.BoolAttribute{Optional: true, MarkdownDescription: "Suppress routine SSH output. Internal readiness diagnostics and failure details remain available. Defaults to false."},
			"pty_allocation":              schema.BoolAttribute{Optional: true, MarkdownDescription: "Force (`-t`) or disable (`-T`) pseudo-terminal allocation. Defaults to false."},
			//"instance_token":              schema.StringAttribute{Computed: true, MarkdownDescription: "internal invocation id"},
		},
		Blocks: map[string]schema.Block{
			"jump_host": schema.ListNestedBlock{
				MarkdownDescription: "Optional jump host (`-J`).",
				Validators:          []validator.List{listvalidator.SizeAtMost(1)},
				NestedObject: schema.NestedBlockObject{
					Attributes: map[string]schema.Attribute{
						"jump_host_destination": schema.StringAttribute{Required: true, MarkdownDescription: "SSH jump destination."},
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
				MarkdownDescription: "Repeatable option (`-o Name=Value`)",
				NestedObject: schema.NestedBlockObject{
					Attributes: map[string]schema.Attribute{
						"name":  schema.StringAttribute{Required: true, MarkdownDescription: "see `man ssh_config`"},
						"value": schema.StringAttribute{Required: true, MarkdownDescription: "see `man ssh_config`"},
					},
				},
			},
		},
	}
}

func (r *sshConnectionResource) Open(ctx context.Context, req ephemeral.OpenRequest, resp *ephemeral.OpenResponse) {
	var data sshConnectionModel
	resp.Diagnostics.Append(req.Config.Get(ctx, &data)...)
	if resp.Diagnostics.HasError() {
		return
	}

	tokenBytes := make([]byte, 16)
	if _, err := rand.Read(tokenBytes); err != nil {
		resp.Diagnostics.AddError("Unable to track SSH process", err.Error())
		return
	}
	token := hex.EncodeToString(tokenBytes)
	resp.Diagnostics.Append(resp.Private.SetKey(ctx, processKey, []byte(strconv.Quote(token)))...)
	if resp.Diagnostics.HasError() {
		return
	}

	conn, err := r.start(ctx, data)
	if err != nil {
		resp.Diagnostics.AddError("Unable to start SSH", err.Error())
		return
	}
	readyCtx, cancel := context.WithTimeout(ctx, 60*time.Second)
	defer cancel()
	if err := conn.WaitReady(readyCtx); err != nil {
		resp.Diagnostics.AddError("SSH connection did not become ready", err.Error())
		if closeErr := conn.Close(); closeErr != nil {
			resp.Diagnostics.AddError("Unable to stop SSH", closeErr.Error())
		}
		return
	}

	r.mu.Lock()
	r.processes[token] = conn
	r.mu.Unlock()
}

func (r *sshConnectionResource) Close(ctx context.Context, req ephemeral.CloseRequest, resp *ephemeral.CloseResponse) {
	raw, diags := req.Private.GetKey(ctx, processKey)
	resp.Diagnostics.Append(diags...)
	if resp.Diagnostics.HasError() || len(raw) == 0 {
		return
	}
	var token string
	if _, err := fmt.Sscanf(string(raw), "%q", &token); err != nil {
		resp.Diagnostics.AddError("Unable to identify SSH process", err.Error())
		return
	}
	r.mu.Lock()
	process, ok := r.processes[token]
	delete(r.processes, token)
	r.mu.Unlock()
	if !ok {
		return
	}
	if err := process.Close(); err != nil {
		resp.Diagnostics.AddError("Unable to stop SSH", err.Error())
	}
}
