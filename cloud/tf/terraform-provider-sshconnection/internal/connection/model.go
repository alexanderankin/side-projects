package connection

import (
	"github.com/hashicorp/terraform-plugin-framework/attr"
	"github.com/hashicorp/terraform-plugin-framework/diag"
	"github.com/hashicorp/terraform-plugin-framework/path"
	"github.com/hashicorp/terraform-plugin-framework/types"
)

// Model holds Terraform values. Null means omitted; unknown means Terraform
// cannot determine the value yet. The tfsdk tags connect fields to HCL names.
type Model struct {
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

type sshOptionModel struct {
	Name  types.String `tfsdk:"name"`
	Value types.String `tfsdk:"value"`
}

// List the fields explicitly so startup can reject unresolved values without
// reflection. When adding a model field, add its name here too.
func modelValues(model Model) map[string]attr.Value {
	return map[string]attr.Value{
		"destination":                 model.Destination,
		"ip_version":                  model.IPVersion,
		"agent_connection_forwarding": model.AgentConnectionForwarding,
		"cipher_spec":                 model.CipherSpec,
		"log_file":                    model.LogFile,
		"config_file":                 model.ConfigFile,
		"identity_file":               model.IdentityFile,
		"jump_host":                   model.JumpHost,
		"listen":                      model.Listen,
		"login_name":                  model.LoginName,
		"mac_spec":                    model.MACSpec,
		"port":                        model.Port,
		"quiet":                       model.Quiet,
		"remote_listen":               model.RemoteListen,
		"pty_allocation":              model.PTYAllocation,
		"ssh_option":                  model.SSHOptions,
	}
}

func requireKnown(value attr.Value, valuePath path.Path, diagnostics *diag.Diagnostics) {
	if value.IsUnknown() {
		diagnostics.AddAttributeError(valuePath,
			"SSH configuration is not known",
			"This value must be known before opening the SSH connection.")
		return
	}
	if value.IsNull() {
		return
	}
	switch value := value.(type) {
	case types.List:
		for index, element := range value.Elements() {
			requireKnown(element, valuePath.AtListIndex(index), diagnostics)
		}
	case types.Object:
		for name, element := range value.Attributes() {
			requireKnown(element, valuePath.AtName(name), diagnostics)
		}
	}
}
