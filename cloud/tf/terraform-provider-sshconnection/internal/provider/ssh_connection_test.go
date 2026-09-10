package provider

import (
	"context"
	"reflect"
	"testing"

	"github.com/hashicorp/terraform-plugin-framework/attr"
	"github.com/hashicorp/terraform-plugin-framework/types"
)

func TestBuildSSHArgs(t *testing.T) {
	ctx := context.Background()
	stringList := func(values ...string) types.List {
		result, diags := types.ListValueFrom(ctx, types.StringType, values)
		if diags.HasError() {
			t.Fatalf("constructing string list: %v", diags)
		}
		return result
	}
	forwardList := func(values ...listenModel) types.List {
		result, diags := types.ListValueFrom(ctx,
			types.ObjectType{
				AttrTypes: map[string]attr.Type{
					"bind_address": types.StringType,
					"port":         types.StringType,
					"host":         types.StringType,
					"host_port":    types.StringType,
				},
			},
			values)
		if diags.HasError() {
			t.Fatalf("constructing forwarding list: %v", diags)
		}
		return result
	}
	jump, diags := types.ListValueFrom(
		ctx,
		types.ObjectType{
			AttrTypes: map[string]attr.Type{"jump_host_destination": types.StringType},
		},
		[]jumpHostModel{{Destination: types.StringValue("bastion")}},
	)
	if diags.HasError() {
		t.Fatalf("constructing jump host: %v", diags)
	}

	data := sshConnectionModel{
		Destination:               types.StringValue("server.example.com"),
		IPVersion:                 types.StringValue("ipv6"),
		AgentConnectionForwarding: types.BoolValue(true),
		CipherSpec:                stringList("chacha20-poly1305@openssh.com", "aes256-gcm@openssh.com"),
		LogFile:                   types.StringValue("ssh.log"),
		ConfigFile:                types.StringValue("ssh_config"),
		IdentityFile:              types.StringValue("id_ed25519"),
		JumpHost:                  jump,
		Listen: forwardList(
			listenModel{BindAddress: types.StringValue("127.0.0.1"), Port: types.StringValue("5432"), Host: types.StringValue("db"), HostPort: types.StringValue("5432")},
			listenModel{BindAddress: types.StringValue(""), Port: types.StringValue("8080"), Host: types.StringValue("web"), HostPort: types.StringValue("80")},
		),
		LoginName:     types.StringValue("deploy"),
		MACSpec:       stringList("hmac-sha2-512", "hmac-sha2-256"),
		Port:          types.Int64Value(2222),
		Quiet:         types.BoolValue(true),
		RemoteListen:  forwardList(listenModel{BindAddress: types.StringNull(), Port: types.StringValue("9000"), Host: types.StringValue("localhost"), HostPort: types.StringValue("9001")}),
		PTYAllocation: types.BoolValue(false),
	}
	want := []string{"-N", "-o", "ExitOnForwardFailure=yes", "-6", "-A", "-c", "chacha20-poly1305@openssh.com,aes256-gcm@openssh.com", "-E", "ssh.log", "-F", "ssh_config", "-i", "id_ed25519", "-J", "bastion", "-L", "127.0.0.1:5432:db:5432", "-L", ":8080:web:80", "-l", "deploy", "-m", "hmac-sha2-512,hmac-sha2-256", "-p", "2222", "-q", "-R", "9000:localhost:9001", "-T", "server.example.com"}

	got, gotDiags := buildSSHArgs(ctx, data)
	if gotDiags.HasError() {
		t.Fatalf("buildSSHArgs diagnostics: %v", gotDiags)
	}
	if !reflect.DeepEqual(got, want) {
		t.Fatalf("arguments differ\n got: %#v\nwant: %#v", got, want)
	}
}
