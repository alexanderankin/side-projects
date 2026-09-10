package connection

import (
	"context"
	"fmt"
	"strings"
	"unicode"

	"github.com/hashicorp/terraform-plugin-framework/diag"
	"github.com/hashicorp/terraform-plugin-framework/path"
	"github.com/hashicorp/terraform-plugin-framework/types"
	"github.com/hashicorp/terraform-plugin-framework/types/basetypes"
)

// These settings are part of how the provider works, not user preferences.
// Keep the reasons here so validation errors explain why a value is required.
type requiredOption struct {
	name   string
	value  string
	reason string
}

var requiredOptions = []requiredOption{
	{"LogLevel", "DEBUG1", "The provider reads SSH debug messages to detect readiness."},
	{"ExitOnForwardFailure", "yes", "A failed forward must fail opening the connection."},
	{"ForkAfterAuthentication", "no", "SSH must stay in the foreground so the supervisor owns its lifetime."},
	{"ControlMaster", "no", "The connection must not create a shared SSH master."},
	{"ControlPath", "none", "The connection must not attach to an existing SSH master."},
	{"ControlPersist", "no", "The connection must not outlive the provider as a persistent master."},
	{"BatchMode", "yes", "Terraform cannot answer interactive authentication prompts."},
	{"ClearAllForwardings", "no", "SSH must not erase the requested forwards."},
	{"SessionType", "none", "This resource runs SSH without a remote command, using -N."},
}

func findRequiredOption(name string) (requiredOption, bool) {
	for _, option := range requiredOptions {
		if strings.EqualFold(name, option.name) {
			return option, true
		}
	}
	return requiredOption{}, false
}

// Validate checks provider-specific rules. Unknown values are normal during
// Terraform planning; buildSSHArgs checks that they are resolved before launch.
func Validate(ctx context.Context, model Model) diag.Diagnostics {
	var diagnostics diag.Diagnostics
	if !model.Destination.IsUnknown() {
		destination := model.Destination.ValueString()
		if strings.TrimSpace(destination) == "" || strings.HasPrefix(destination, "-") {
			diagnostics.AddAttributeError(path.Root("destination"), "Invalid SSH destination", "Provide a non-empty SSH destination that does not begin with '-'.")
		}
	}
	if !model.ConfigFile.IsNull() && !model.ConfigFile.IsUnknown() && !strings.EqualFold(model.ConfigFile.ValueString(), "none") {
		diagnostics.AddAttributeError(path.Root("config_file"), "External SSH configuration is disabled", "The provider always uses -F none. Remove config_file and move host names, users, ports, identities, and forwarding settings into Terraform attributes or ssh_option blocks.")
	}
	if model.SSHOptions.IsNull() || model.SSHOptions.IsUnknown() {
		return diagnostics
	}
	// Decode each block separately so a known conflict is still caught when a
	// different block (or just its value) is not known until apply.
	for index, element := range model.SSHOptions.Elements() {
		if element.IsUnknown() {
			continue
		}
		optionPath := path.Root("ssh_option").AtListIndex(index)
		object, ok := element.(types.Object)
		if !ok || object.IsNull() {
			diagnostics.AddAttributeError(optionPath, "Invalid SSH option", "Each ssh_option must contain a name and value.")
			continue
		}
		var option sshOptionModel
		decodeDiagnostics := object.As(ctx, &option, basetypes.ObjectAsOptions{})
		diagnostics.Append(decodeDiagnostics...)
		if decodeDiagnostics.HasError() {
			continue
		}
		if option.Name.IsUnknown() {
			continue
		}
		name := option.Name.ValueString()
		if name == "" || strings.ContainsAny(name, "=\x00") || strings.IndexFunc(name, unicode.IsSpace) >= 0 {
			diagnostics.AddAttributeError(optionPath.AtName("name"), "Invalid SSH option name", "Use one SSH option keyword without whitespace or '='; put its argument in value.")
			continue
		}
		switch strings.ToLower(name) {
		case "include", "host", "match":
			diagnostics.AddAttributeError(optionPath.AtName("name"), "SSH configuration directive is unsupported", "External SSH configuration is disabled. Set connection options directly in Terraform instead of using Include, Host, or Match.")
			continue
		}
		if option.Value.IsUnknown() {
			continue
		}
		if option.Value.IsNull() {
			diagnostics.AddAttributeError(optionPath.AtName("value"), "Missing SSH option value", "Each ssh_option requires a value.")
			continue
		}
		if required, found := findRequiredOption(name); found {
			value := option.Value.ValueString()
			matches := strings.EqualFold(value, required.value)
			if required.name == "LogLevel" && strings.EqualFold(value, "DEBUG") {
				matches = true
			}
			if !matches {
				diagnostics.AddAttributeError(optionPath.AtName("value"), "SSH option conflicts with provider requirements", fmt.Sprintf("%s must be %s. %s Remove this block or use the required value.", required.name, required.value, required.reason))
			}
		}
	}
	return diagnostics
}
