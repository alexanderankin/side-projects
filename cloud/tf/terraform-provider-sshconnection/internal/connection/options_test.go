package connection

import (
	"context"
	"os/exec"
	"strings"
	"testing"

	"github.com/hashicorp/terraform-plugin-framework/attr"
	"github.com/hashicorp/terraform-plugin-framework/diag"
	"github.com/hashicorp/terraform-plugin-framework/types"
)

func optionList(t *testing.T, options ...sshOptionModel) types.List {
	t.Helper()
	list, diagnostics := types.ListValueFrom(context.Background(), types.ObjectType{AttrTypes: map[string]attr.Type{"name": types.StringType, "value": types.StringType}}, options)
	if diagnostics.HasError() {
		t.Fatal(diagnostics)
	}
	return list
}

func TestRequiredOptions(t *testing.T) {
	for _, required := range requiredOptions {
		t.Run(required.name, func(t *testing.T) {
			model := Model{Destination: types.StringValue("example.invalid")}
			model.SSHOptions = optionList(t, sshOptionModel{Name: types.StringValue(strings.ToLower(required.name)), Value: types.StringValue(required.value)})
			args, diagnostics := buildSSHArgs(context.Background(), model)
			if diagnostics.HasError() {
				t.Fatal(diagnostics)
			}
			count := 0
			for _, arg := range args {
				if strings.EqualFold(arg, required.name+"="+required.value) {
					count++
				}
			}
			expected := 1
			if required.name == "SessionType" {
				expected = 0
			} // Set by -N.
			if count != expected {
				t.Fatalf("required option emitted %d times: %v", count, args)
			}

			// The second occurrence must be checked even though the first one matches.
			model.SSHOptions = optionList(t,
				sshOptionModel{Name: types.StringValue(required.name), Value: types.StringValue(required.value)},
				sshOptionModel{Name: types.StringValue(required.name), Value: types.StringValue("conflicting-value")},
			)
			args, diagnostics = buildSSHArgs(context.Background(), model)
			if !diagnostics.HasError() || args != nil {
				t.Fatalf("conflict accepted: %v %v", args, diagnostics)
			}
			if !strings.Contains(diagnostics[0].Detail(), required.reason) {
				t.Fatal(diagnostics)
			}
			withPath, ok := diagnostics[0].(diag.DiagnosticWithPath)
			if !ok || withPath.Path().String() != "ssh_option[1].value" {
				t.Fatalf("missing block location: %v", diagnostics)
			}
		})
	}
}

func TestOptionValidation(t *testing.T) {
	for _, name := range []string{"", "LogLevel=QUIET", "LogLevel DEBUG1", "LogLevel\n", "Include", "hOSt", "Match"} {
		t.Run(name, func(t *testing.T) {
			model := Model{Destination: types.StringValue("example.invalid"), SSHOptions: optionList(t, sshOptionModel{Name: types.StringValue(name), Value: types.StringValue("x")})}
			if !Validate(context.Background(), model).HasError() {
				t.Fatal("invalid option accepted")
			}
		})
	}
	model := Model{Destination: types.StringValue("example.invalid"), SSHOptions: optionList(t,
		sshOptionModel{Name: types.StringValue("LogLevel"), Value: types.StringValue("DEBUG")},
		sshOptionModel{Name: types.StringValue("IdentityFile"), Value: types.StringValue("key one")},
		sshOptionModel{Name: types.StringValue("IdentityFile"), Value: types.StringValue("key two")},
	)}
	args, diagnostics := buildSSHArgs(context.Background(), model)
	if diagnostics.HasError() {
		t.Fatal(diagnostics)
	}
	if !strings.Contains(strings.Join(args, "|"), "-o|IdentityFile=key one|-o|IdentityFile=key two|--|example.invalid") {
		t.Fatal(args)
	}
}

func TestUnknownValues(t *testing.T) {
	model := Model{Destination: types.StringUnknown(), SSHOptions: optionList(t,
		sshOptionModel{Name: types.StringValue("BatchMode"), Value: types.StringUnknown()},
	)}
	if diagnostics := Validate(context.Background(), model); diagnostics.HasError() {
		t.Fatalf("planning rejected unknowns: %v", diagnostics)
	}
	if args, diagnostics := buildSSHArgs(context.Background(), model); !diagnostics.HasError() || args != nil {
		t.Fatalf("startup accepted unknowns: %v", args)
	}
	// Unknown fields must not prevent a known conflict in another block being reported.
	model.SSHOptions = optionList(t,
		sshOptionModel{Name: types.StringValue("BatchMode"), Value: types.StringUnknown()},
		sshOptionModel{Name: types.StringValue("ClearAllForwardings"), Value: types.StringValue("yes")},
	)
	if !Validate(context.Background(), model).HasError() {
		t.Fatal("known conflict missed")
	}
	model = Model{Destination: types.StringValue("example.invalid"), LogFile: types.StringUnknown()}
	if _, diagnostics := buildSSHArgs(context.Background(), model); !diagnostics.HasError() {
		t.Fatal("unknown optional value accepted at startup")
	}
}

func TestConfigAndDestinationValidation(t *testing.T) {
	for _, file := range []types.String{types.StringNull(), types.StringValue("none"), types.StringValue("NONE")} {
		if diagnostics := Validate(context.Background(), Model{Destination: types.StringValue("example.invalid"), ConfigFile: file}); diagnostics.HasError() {
			t.Fatal(diagnostics)
		}
	}
	for _, file := range []string{"", "~/.ssh/config", "/tmp/custom-config"} {
		if !Validate(context.Background(), Model{Destination: types.StringValue("example.invalid"), ConfigFile: types.StringValue(file)}).HasError() {
			t.Fatalf("accepted external config %q", file)
		}
	}
	for _, destination := range []string{"", " ", "-F"} {
		if !Validate(context.Background(), Model{Destination: types.StringValue(destination)}).HasError() {
			t.Fatalf("accepted destination %q", destination)
		}
	}
}

func TestForwardAddress(t *testing.T) {
	for input, want := range map[string]string{"": "", "localhost": "localhost", "127.0.0.1": "127.0.0.1", "::1": "[::1]", "[::1]": "[::1]", "fe80::1%lo0": "[fe80::1%lo0]"} {
		if got := forwardAddress(input); got != want {
			t.Fatalf("%q: got %q, want %q", input, got, want)
		}
	}
}

func TestEffectiveSSHConfiguration(t *testing.T) {
	if _, err := exec.LookPath("ssh"); err != nil {
		t.Skip("OpenSSH is required")
	}
	jump, diagnostics := types.ListValueFrom(context.Background(), types.ObjectType{AttrTypes: map[string]attr.Type{"jump_host_destination": types.StringType}}, []jumpHostModel{{Destination: types.StringValue("jump.example.invalid")}})
	if diagnostics.HasError() {
		t.Fatal(diagnostics)
	}
	args, diagnostics := buildSSHArgs(context.Background(), Model{Destination: types.StringValue("example.invalid"), JumpHost: jump})
	if diagnostics.HasError() {
		t.Fatal(diagnostics)
	}
	// -G prints configuration without connecting or resolving the destination.
	command := exec.Command("ssh", append([]string{"-G", "-v"}, args...)...)
	output, err := command.CombinedOutput()
	if err != nil {
		t.Fatalf("ssh -G: %v\n%s", err, output)
	}
	text := string(output)
	for _, want := range []string{"batchmode yes", "exitonforwardfailure yes", "clearallforwardings no", "sessiontype none", "controlpersist no", "loglevel DEBUG", " -F none"} {
		if !strings.Contains(text, want) {
			t.Fatalf("missing %q:\n%s", want, text)
		}
	}
	if strings.Contains(text, "Reading configuration data") {
		t.Fatalf("SSH read external config:\n%s", text)
	}
}

func TestJumpHostLimitAtStartup(t *testing.T) {
	jumps, diagnostics := types.ListValueFrom(context.Background(), types.ObjectType{AttrTypes: map[string]attr.Type{"jump_host_destination": types.StringType}}, []jumpHostModel{
		{Destination: types.StringValue("first")}, {Destination: types.StringValue("second")},
	})
	if diagnostics.HasError() {
		t.Fatal(diagnostics)
	}
	args, diagnostics := buildSSHArgs(context.Background(), Model{Destination: types.StringValue("example.invalid"), JumpHost: jumps})
	if args != nil || !diagnostics.HasError() {
		t.Fatalf("silently ignored jumps: %v", args)
	}
}

func TestIPv6ForwardArgumentsWithOpenSSH(t *testing.T) {
	if _, err := exec.LookPath("ssh"); err != nil {
		t.Skip("OpenSSH is required")
	}
	forwards, diagnostics := types.ListValueFrom(context.Background(), types.ObjectType{AttrTypes: map[string]attr.Type{
		"bind_address": types.StringType, "port": types.StringType, "host": types.StringType, "host_port": types.StringType,
	}}, []listenModel{{BindAddress: types.StringValue("::1"), Port: types.StringValue("18080"), Host: types.StringValue("[::1]"), HostPort: types.StringValue("80")}})
	if diagnostics.HasError() {
		t.Fatal(diagnostics)
	}
	args, diagnostics := buildSSHArgs(context.Background(), Model{Destination: types.StringValue("example.invalid"), Listen: forwards})
	if diagnostics.HasError() {
		t.Fatal(diagnostics)
	}
	output, err := exec.Command("ssh", append([]string{"-G"}, args...)...).CombinedOutput()
	if err != nil {
		t.Fatalf("SSH rejected IPv6 forward: %v\n%s", err, output)
	}
	if !strings.Contains(string(output), "localforward [::1]:18080 [::1]:80") {
		t.Fatalf("forward changed: %s", output)
	}
}
