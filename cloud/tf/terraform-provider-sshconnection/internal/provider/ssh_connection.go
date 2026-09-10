package provider

import (
	"bytes"
	"context"
	"crypto/rand"
	"encoding/hex"
	"errors"
	"fmt"
	"io"
	"os"
	"os/exec"
	"strconv"
	"strings"
	"sync"
	"time"

	"github.com/hashicorp/terraform-plugin-framework-validators/listvalidator"
	"github.com/hashicorp/terraform-plugin-framework-validators/stringvalidator"
	"github.com/hashicorp/terraform-plugin-framework/diag"
	"github.com/hashicorp/terraform-plugin-framework/ephemeral"
	"github.com/hashicorp/terraform-plugin-framework/ephemeral/schema"
	"github.com/hashicorp/terraform-plugin-framework/schema/validator"
	"github.com/hashicorp/terraform-plugin-framework/types"
)

const processKey = "ssh_connection_provider_process_id"

var _ ephemeral.EphemeralResource = (*sshConnectionResource)(nil)
var _ ephemeral.EphemeralResourceWithClose = (*sshConnectionResource)(nil)

type commandProcess struct {
	cmd       *exec.Cmd
	keepalive io.Closer
	done      <-chan error
}

type sshConnectionResource struct {
	mu        sync.Mutex
	processes map[string]commandProcess
}

type sshConnectionModel struct {
	Destination               types.String `tfsdk:"destination"`
	IPVersion                 types.String `tfsdk:"ip_version"`
	AgentConnectionForwarding types.Bool   `tfsdk:"agent_connection_forwarding"`
	CipherSpec                types.List   `tfsdk:"cipher_spec"`
	LogFile                   types.String `tfsdk:"log_file"`
	ConfigFile                types.String `tfsdk:"config_file"`
	IdentityFile              types.String `tfsdk:"identity_file"`
	JumpHost                  types.List   `tfsdk:"jump_host"`
	Listen                    types.List   `tfsdk:"listen"`
	LoginName                 types.String `tfsdk:"login_name"`
	MACSpec                   types.List   `tfsdk:"mac_spec"`
	Port                      types.Int64  `tfsdk:"port"`
	Quiet                     types.Bool   `tfsdk:"quiet"`
	RemoteListen              types.List   `tfsdk:"remote_listen"`
	PTYAllocation             types.Bool   `tfsdk:"pty_allocation"`
	SSHOptions                types.List   `tfsdk:"ssh_option"`
	//InstanceToken             types.String `tfsdk:"instance_token"`
}

type jumpHostModel struct {
	Destination types.String `tfsdk:"jump_host_destination"`
}

type listenModel struct {
	BindAddress types.String `tfsdk:"bind_address"`
	Port        types.String `tfsdk:"port"`
	Host        types.String `tfsdk:"host"`
	HostPort    types.String `tfsdk:"host_port"`
}

func NewSSHConnection() ephemeral.EphemeralResource {
	return &sshConnectionResource{processes: make(map[string]commandProcess)}
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
		MarkdownDescription: "Starts `ssh -N` when opened and stops it when the ephemeral resource is closed. SSH must be available on `PATH`.",
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
			"quiet":                       schema.BoolAttribute{Optional: true, MarkdownDescription: "Enable quiet mode (`-q`). Defaults to false."},
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

	args, diags := buildSSHArgs(ctx, data)
	resp.Diagnostics.Append(diags...)
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

	keepaliveReader, keepaliveWriter, err := os.Pipe()
	if err != nil {
		resp.Diagnostics.AddError("Unable to create SSH supervisor", err.Error())
		return
	}
	executable, err := os.Executable()
	if err != nil {
		_ = keepaliveReader.Close()
		_ = keepaliveWriter.Close()
		resp.Diagnostics.AddError("Unable to locate SSH supervisor", err.Error())
		return
	}

	var stderr, stdout bytes.Buffer
	cmd := exec.Command(executable, append([]string{"--ssh-supervisor"}, args...)...)
	cmd.Stdin = keepaliveReader
	cmd.Stderr = &stderr
	cmd.Stdout = &stdout
	if err := cmd.Start(); err != nil {
		_ = keepaliveReader.Close()
		_ = keepaliveWriter.Close()
		resp.Diagnostics.AddError("Unable to start SSH", err.Error())
		return
	}
	_ = keepaliveReader.Close()

	done := make(chan error, 1)
	go func() { done <- cmd.Wait() }()
	select {
	case err := <-done:
		_ = keepaliveWriter.Close()
		resp.Diagnostics.AddError("SSH connection exited", fmt.Sprintf("ssh exited before the connection could be opened: %v\n%s", err, strings.TrimSpace(stderr.String())))
		return
	case <-time.After(200 * time.Millisecond):
	}

	r.mu.Lock()
	r.processes[token] = commandProcess{cmd: cmd, keepalive: keepaliveWriter, done: done}
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
	if err := process.keepalive.Close(); err != nil && !errors.Is(err, os.ErrClosed) {
		resp.Diagnostics.AddError("Unable to signal SSH supervisor", err.Error())
		return
	}
	if err := <-process.done; err != nil {
		resp.Diagnostics.AddError("Unable to stop SSH", err.Error())
	}
}

func buildSSHArgs(ctx context.Context, data sshConnectionModel) ([]string, diag.Diagnostics) {
	var diags diag.Diagnostics
	args := []string{"-N", "-o", "ExitOnForwardFailure=yes"}

	switch data.IPVersion.ValueString() {
	case "ipv4":
		args = append(args, "-4")
	case "ipv6":
		args = append(args, "-6")
	}
	if data.AgentConnectionForwarding.ValueBool() {
		args = append(args, "-A")
	} else {
		args = append(args, "-a")
	}
	args = appendStringList(ctx, args, "-c", data.CipherSpec, &diags)
	args = appendOptional(args, "-E", data.LogFile)
	args = appendOptional(args, "-F", data.ConfigFile)
	args = appendOptional(args, "-i", data.IdentityFile)
	if !data.JumpHost.IsNull() && !data.JumpHost.IsUnknown() {
		var jumps []jumpHostModel
		diags.Append(data.JumpHost.ElementsAs(ctx, &jumps, false)...)
		if !diags.HasError() && len(jumps) == 1 {
			args = append(args, "-J", jumps[0].Destination.ValueString())
		}
	}
	args = appendForwards(ctx, args, "-L", data.Listen, &diags)
	args = appendOptional(args, "-l", data.LoginName)
	args = appendStringList(ctx, args, "-m", data.MACSpec, &diags)
	if !data.Port.IsNull() && !data.Port.IsUnknown() {
		args = append(args, "-p", strconv.FormatInt(data.Port.ValueInt64(), 10))
	}
	if data.Quiet.ValueBool() {
		args = append(args, "-q")
	}
	args = appendForwards(ctx, args, "-R", data.RemoteListen, &diags)
	if data.PTYAllocation.ValueBool() {
		args = append(args, "-t")
	} else {
		args = append(args, "-T")
	}
	args = appendSshOptions(ctx, args, data.SSHOptions, &diags)
	args = append(args, data.Destination.ValueString())
	return args, diags
}

func appendOptional(args []string, flag string, value types.String) []string {
	if value.IsNull() || value.IsUnknown() {
		return args
	}
	return append(args, flag, value.ValueString())
}

func appendStringList(ctx context.Context, args []string, flag string, value types.List, diags *diag.Diagnostics) []string {
	if value.IsNull() || value.IsUnknown() {
		return args
	}
	var values []string
	diags.Append(value.ElementsAs(ctx, &values, false)...)
	if len(values) > 0 {
		args = append(args, flag, strings.Join(values, ","))
	}
	return args
}

func appendForwards(ctx context.Context, args []string, flag string, value types.List, diags *diag.Diagnostics) []string {
	if value.IsNull() || value.IsUnknown() {
		return args
	}
	var forwards []listenModel
	diags.Append(value.ElementsAs(ctx, &forwards, false)...)
	for _, forward := range forwards {
		parts := make([]string, 0, 4)
		if !forward.BindAddress.IsNull() {
			parts = append(parts, forward.BindAddress.ValueString())
		}
		parts = append(parts, forward.Port.ValueString(), forward.Host.ValueString(), forward.HostPort.ValueString())
		args = append(args, flag, strings.Join(parts, ":"))
	}
	return args
}

type sshOptionModel struct {
	Name  types.String `tfsdk:"name"`
	Value types.String `tfsdk:"value"`
}

func appendSshOptions(ctx context.Context, args []string, value types.List, diags *diag.Diagnostics) []string {
	if value.IsNull() || value.IsUnknown() {
		return args
	}
	var options []sshOptionModel
	diags.Append(value.ElementsAs(ctx, &options, false)...)
	for _, option := range options {
		args = append(args, "-o", option.Name.ValueString()+"="+option.Value.ValueString())
	}
	return args
}
