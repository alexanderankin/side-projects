package connection

import "github.com/hashicorp/terraform-plugin-framework/types"

// Model is the decoded connection configuration shared by connection backends.
// Terraform null/unknown values are retained until a backend interprets them.
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
