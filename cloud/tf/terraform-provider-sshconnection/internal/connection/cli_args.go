package connection

import (
	"context"
	"strconv"
	"strings"

	"github.com/hashicorp/terraform-plugin-framework/diag"
	"github.com/hashicorp/terraform-plugin-framework/path"
	"github.com/hashicorp/terraform-plugin-framework/types"
)

func buildSSHArgs(ctx context.Context, model Model) ([]string, diag.Diagnostics) {
	diagnostics := Validate(ctx, model)
	// Refuse to turn unresolved values into empty strings or false at launch.
	for name, value := range modelValues(model) {
		requireKnown(value, path.Root(name), &diagnostics)
	}
	if diagnostics.HasError() {
		return nil, diagnostics
	}

	args := []string{"-F", "none", "-N"}
	for _, option := range requiredOptions {
		if option.name != "SessionType" { // -N already sets SessionType=none.
			args = append(args, "-o", option.name+"="+option.value)
		}
	}

	switch model.IPVersion.ValueString() {
	case "ipv4":
		args = append(args, "-4")
	case "ipv6":
		args = append(args, "-6")
	}
	if model.AgentConnectionForwarding.ValueBool() {
		args = append(args, "-A")
	} else {
		args = append(args, "-a")
	}
	args = appendStringList(ctx, args, "-c", model.CipherSpec, &diagnostics)
	args = appendOptional(args, "-i", model.IdentityFile)
	if !model.JumpHost.IsNull() && !model.JumpHost.IsUnknown() {
		var jumps []jumpHostModel
		diagnostics.Append(model.JumpHost.ElementsAs(ctx, &jumps, false)...)
		if diagnostics.HasError() {
			return nil, diagnostics
		}
		if len(jumps) > 1 {
			diagnostics.AddAttributeError(path.Root("jump_host"), "Too many jump_host blocks", "Use one jump_host block; its destination may contain a comma-separated SSH jump chain.")
			return nil, diagnostics
		}
		if len(jumps) == 1 {
			args = append(args, "-J", jumps[0].Destination.ValueString())
		}
	}
	args = appendForwards(ctx, args, "-L", model.Listen, &diagnostics)
	args = appendOptional(args, "-l", model.LoginName)
	args = appendStringList(ctx, args, "-m", model.MACSpec, &diagnostics)
	if !model.Port.IsNull() && !model.Port.IsUnknown() {
		args = append(args, "-p", strconv.FormatInt(model.Port.ValueInt64(), 10))
	}
	args = appendForwards(ctx, args, "-R", model.RemoteListen, &diagnostics)
	if model.PTYAllocation.ValueBool() {
		args = append(args, "-t")
	} else {
		args = append(args, "-T")
	}
	args = appendSSHOptions(ctx, args, model.SSHOptions, &diagnostics)
	if diagnostics.HasError() {
		return nil, diagnostics
	}
	args = append(args, "--", model.Destination.ValueString())
	return args, diagnostics
}

func appendOptional(args []string, flag string, value types.String) []string {
	if value.IsNull() || value.IsUnknown() {
		return args
	}
	return append(args, flag, value.ValueString())
}

func appendStringList(ctx context.Context, args []string, flag string, value types.List, diagnostics *diag.Diagnostics) []string {
	if value.IsNull() || value.IsUnknown() {
		return args
	}
	var values []string
	diagnostics.Append(value.ElementsAs(ctx, &values, false)...)
	if diagnostics.HasError() {
		return args
	}
	if len(values) > 0 {
		args = append(args, flag, strings.Join(values, ","))
	}
	return args
}

func appendForwards(ctx context.Context, args []string, flag string, value types.List, diagnostics *diag.Diagnostics) []string {
	if value.IsNull() || value.IsUnknown() {
		return args
	}
	var forwards []listenModel
	diagnostics.Append(value.ElementsAs(ctx, &forwards, false)...)
	if diagnostics.HasError() {
		return args
	}
	for _, forward := range forwards {
		parts := make([]string, 0, 4)
		if !forward.BindAddress.IsNull() {
			parts = append(parts, forwardAddress(forward.BindAddress.ValueString()))
		}
		parts = append(parts, forward.Port.ValueString(), forwardAddress(forward.Host.ValueString()), forward.HostPort.ValueString())
		args = append(args, flag, strings.Join(parts, ":"))
	}
	return args
}

func appendSSHOptions(ctx context.Context, args []string, value types.List, diagnostics *diag.Diagnostics) []string {
	if value.IsNull() || value.IsUnknown() {
		return args
	}
	var options []sshOptionModel
	diagnostics.Append(value.ElementsAs(ctx, &options, false)...)
	if diagnostics.HasError() {
		return args
	}
	for _, option := range options {
		if _, required := findRequiredOption(option.Name.ValueString()); required {
			continue
		}
		args = append(args, "-o", option.Name.ValueString()+"="+option.Value.ValueString())
	}
	return args
}

// IPv6 colons must be bracketed inside SSH's colon-separated forward syntax.
func forwardAddress(address string) string {
	if strings.Contains(address, ":") && !strings.HasPrefix(address, "[") {
		return "[" + address + "]"
	}
	return address
}
