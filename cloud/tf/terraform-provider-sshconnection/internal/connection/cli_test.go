package connection

import (
	"context"
	"crypto/ed25519"
	"crypto/rand"
	"errors"
	"fmt"
	"io"
	"net"
	"os/exec"
	"path/filepath"
	"strings"
	"testing"
	"time"

	"github.com/hashicorp/terraform-plugin-framework/attr"
	"github.com/hashicorp/terraform-plugin-framework/types"
	"golang.org/x/crypto/ssh"
)

// Use the real OpenSSH executable and provider supervisor against an in-process
// SSH server. No external hosts, credentials, persistent state, or sleeps in HCL.
func TestCLIReadiness(t *testing.T) {
	if _, err := exec.LookPath("ssh"); err != nil {
		t.Skip("OpenSSH is required")
	}
	executable := filepath.Join(t.TempDir(), "provider")
	build := exec.Command("go", "build", "-o", executable, "../..")
	if out, err := build.CombinedOutput(); err != nil {
		t.Fatalf("build: %v\n%s", err, out)
	}
	_, key, err := ed25519.GenerateKey(rand.Reader)
	if err != nil {
		t.Fatal(err)
	}
	signer, err := ssh.NewSignerFromKey(key)
	if err != nil {
		t.Fatal(err)
	}

	for _, scenario := range []struct {
		name                                           string
		remote, reject, occupied, cancel, customOption bool
	}{
		{name: "local_forward"},
		{name: "delayed_remote_forward", remote: true},
		{name: "remote_forward_via_custom_option", remote: true, customOption: true},
		{name: "remote_forward_denied", remote: true, reject: true},
		{name: "local_port_already_owned", occupied: true},
		{name: "cancel_while_remote_forward_pending", remote: true, cancel: true},
	} {
		t.Run(scenario.name, func(t *testing.T) {
			server, err := net.Listen("tcp", "127.0.0.1:0")
			if err != nil {
				t.Fatal(err)
			}
			defer func() { _ = server.Close() }()
			forward, err := net.Listen("tcp", "127.0.0.1:0")
			if err != nil {
				t.Fatal(err)
			}
			localPort := forward.Addr().(*net.TCPAddr).Port
			if !scenario.occupied {
				_ = forward.Close()
			}
			defer func() { _ = forward.Close() }()
			pending := make(chan struct{})
			release := make(chan struct{})
			defer close(release)
			serverDone := make(chan struct{})
			go func() {
				defer close(serverDone)
				raw, err := server.Accept()
				if err != nil {
					return
				}
				defer func() { _ = raw.Close() }()
				config := &ssh.ServerConfig{NoClientAuth: true}
				config.AddHostKey(signer)
				conn, channels, requests, err := ssh.NewServerConn(raw, config)
				if err != nil {
					return
				}
				defer func() { _ = conn.Close() }()
				disconnected := make(chan struct{})
				go func() { _ = conn.Wait(); close(disconnected) }()
				go func() {
					for ch := range channels {
						if ch.ChannelType() != "direct-tcpip" {
							_ = ch.Reject(ssh.UnknownChannelType, "unsupported")
							continue
						}
						stream, reqs, err := ch.Accept()
						if err != nil {
							continue
						}
						go ssh.DiscardRequests(reqs)
						_, _ = io.WriteString(stream, "ready\n")
						_ = stream.Close()
					}
				}()
				for req := range requests {
					if req.Type != "tcpip-forward" {
						_ = req.Reply(false, nil)
						continue
					}
					close(pending)
					select {
					case <-release:
					case <-disconnected:
						return
					}
					_ = req.Reply(!scenario.reject, nil)
				}
			}()
			listValue := func(attributeTypes map[string]attr.Type, values any) types.List {
				list, diags := types.ListValueFrom(context.Background(), types.ObjectType{AttrTypes: attributeTypes}, values)
				if diags.HasError() {
					t.Fatal(diags)
				}
				return list
			}
			forwardTypes := map[string]attr.Type{"bind_address": types.StringType, "port": types.StringType, "host": types.StringType, "host_port": types.StringType}
			forwardModel := listenModel{BindAddress: types.StringValue("127.0.0.1"), Port: types.StringValue(fmt.Sprint(localPort)), Host: types.StringValue("target"), HostPort: types.StringValue("80")}
			model := Model{
				Destination: types.StringValue("127.0.0.1"),
				ConfigFile:  types.StringValue("none"),
				Port:        types.Int64Value(int64(server.Addr().(*net.TCPAddr).Port)),
				Listen:      listValue(forwardTypes, []listenModel{forwardModel}),
				SSHOptions: listValue(map[string]attr.Type{"name": types.StringType, "value": types.StringType}, []sshOptionModel{
					{Name: types.StringValue("StrictHostKeyChecking"), Value: types.StringValue("no")},
					{Name: types.StringValue("UserKnownHostsFile"), Value: types.StringValue("/dev/null")},
				}),
				// Quiet and file logging must not disable internal readiness detection.
				Quiet: types.BoolValue(true), LogFile: types.StringValue(filepath.Join(t.TempDir(), "ssh.log")),
			}
			if scenario.remote {
				forwardModel.Port = types.StringValue("18081")
				if scenario.customOption {
					extraOption := optionList(t, sshOptionModel{Name: types.StringValue("RemoteForward"), Value: types.StringValue("18081 target:80")})
					elements := append(model.SSHOptions.Elements(), extraOption.Elements()...)
					model.SSHOptions, _ = types.ListValue(model.SSHOptions.ElementType(context.Background()), elements)
				} else {
					model.RemoteListen = listValue(forwardTypes, []listenModel{forwardModel})
				}
			}
			conn, err := startSupervisor(context.Background(), executable, model)
			if err != nil {
				t.Fatal(err)
			}
			defer func() { _ = conn.Close() }()
			ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
			defer cancel()
			ready := make(chan error, 1)
			go func() { ready <- conn.WaitReady(ctx) }()
			if scenario.remote {
				select {
				case <-pending:
				case err := <-ready:
					t.Fatalf("readiness returned before forward request: %v", err)
				case <-ctx.Done():
					t.Fatal("server never received forward request")
				}
				select {
				case err := <-ready:
					t.Fatalf("readiness returned before server acknowledgement: %v", err)
				case <-time.After(250 * time.Millisecond):
				}
				if scenario.cancel {
					cancel()
				} else {
					release <- struct{}{}
				}
			}
			err = <-ready
			switch {
			case scenario.cancel:
				if !errors.Is(err, context.Canceled) {
					t.Fatalf("expected cancellation: %v", err)
				}
			case scenario.reject || scenario.occupied:
				if err == nil || !strings.Contains(err.Error(), "forwarding") {
					t.Fatalf("expected forwarding error: %v", err)
				}
			default:
				if err != nil {
					t.Fatal(err)
				}
				probe, err := net.DialTimeout("tcp", fmt.Sprintf("127.0.0.1:%d", localPort), time.Second)
				if err != nil {
					t.Fatalf("listener not ready: %v", err)
				}
				_ = probe.SetDeadline(time.Now().Add(time.Second))
				out, err := io.ReadAll(probe)
				_ = probe.Close()
				if err != nil || string(out) != "ready\n" {
					t.Fatalf("forward unusable: %q %v", out, err)
				}
			}
			if err := conn.Close(); err != nil {
				t.Fatal(err)
			}
			if err := conn.Close(); err != nil {
				t.Fatal("second close:", err)
			}
			select {
			case <-serverDone:
			case <-time.After(3 * time.Second):
				t.Fatal("SSH connection survived Close")
			}
			if !scenario.occupied {
				listener, err := net.Listen("tcp", fmt.Sprintf("127.0.0.1:%d", localPort))
				if err != nil {
					t.Fatal("SSH listener survived Close:", err)
				}
				_ = listener.Close()
			}
		})
	}
}
