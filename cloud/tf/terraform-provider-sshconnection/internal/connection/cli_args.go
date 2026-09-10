package connection

import (
	"context"
	"github.com/hashicorp/terraform-plugin-framework/diag"
	"github.com/hashicorp/terraform-plugin-framework/types"
	"strconv"
	"strings"
)

func buildSSHArgs(ctx context.Context, data Model) ([]string, diag.Diagnostics) {
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
