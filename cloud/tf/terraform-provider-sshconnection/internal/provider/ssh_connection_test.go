package provider

import (
	"context"
	"encoding/json"
	"errors"
	"strings"
	"sync"
	"testing"
	"time"

	"github.com/hashicorp/terraform-plugin-framework/ephemeral"
	"github.com/hashicorp/terraform-plugin-framework/providerserver"
	"github.com/hashicorp/terraform-plugin-go/tfprotov6"
	"github.com/hashicorp/terraform-plugin-go/tftypes"
	"github.com/toor/terraform-provider-ssh-connection/internal/connection"
)

// Exercise the real framework RPC boundary, including its private-data encoding.
// Only SSH itself is replaced, so these tests need no credentials or network.
type testProvider struct {
	sshConnectionProvider
	resource *sshConnectionResource
}

func (provider *testProvider) EphemeralResources(context.Context) []func() ephemeral.EphemeralResource {
	return []func() ephemeral.EphemeralResource{func() ephemeral.EphemeralResource { return provider.resource }}
}

type fakeConnection struct {
	wait     func(context.Context) error
	closeErr error
	closes   int
}

func (connection *fakeConnection) WaitReady(ctx context.Context) error {
	if connection.wait != nil {
		return connection.wait(ctx)
	}
	return nil
}
func (connection *fakeConnection) Close() error { connection.closes++; return connection.closeErr }

func testServer(t *testing.T, resource *sshConnectionResource) (tfprotov6.ProviderServer, *tfprotov6.DynamicValue) {
	t.Helper()
	server := providerserver.NewProtocol6(&testProvider{resource: resource})()
	schema, err := server.GetProviderSchema(context.Background(), &tfprotov6.GetProviderSchemaRequest{})
	if err != nil {
		t.Fatal(err)
	}
	requireNoErrors(t, schema.Diagnostics)
	objectType := schema.EphemeralResourceSchemas["sshconnection_connection"].ValueType().(tftypes.Object)
	values := make(map[string]tftypes.Value)
	for name, attributeType := range objectType.AttributeTypes {
		values[name] = tftypes.NewValue(attributeType, nil)
	}
	values["destination"] = tftypes.NewValue(tftypes.String, "example.invalid")
	config, err := tfprotov6.NewDynamicValue(objectType, tftypes.NewValue(objectType, values))
	if err != nil {
		t.Fatal(err)
	}
	return server, &config
}

func requireNoErrors(t *testing.T, diagnostics []*tfprotov6.Diagnostic) {
	t.Helper()
	for _, diagnostic := range diagnostics {
		if diagnostic.Severity == tfprotov6.DiagnosticSeverityError {
			t.Fatalf("%s: %s", diagnostic.Summary, diagnostic.Detail)
		}
	}
}

func diagnosticText(diagnostics []*tfprotov6.Diagnostic) string {
	var text strings.Builder
	for _, diagnostic := range diagnostics {
		if diagnostic.Severity == tfprotov6.DiagnosticSeverityError {
			text.WriteString(diagnostic.Summary + ": " + diagnostic.Detail)
		}
	}
	return text.String()
}

func TestConnectionLifecycle(t *testing.T) {
	resource := NewSSHConnection().(*sshConnectionResource)
	var connections []*fakeConnection
	resource.start = func(context.Context, connection.Model) (connection.Connection, error) {
		conn := &fakeConnection{}
		connections = append(connections, conn)
		return conn, nil
	}
	server, config := testServer(t, resource)
	var private [][]byte
	for range 2 {
		response, err := server.OpenEphemeralResource(context.Background(), &tfprotov6.OpenEphemeralResourceRequest{TypeName: "sshconnection_connection", Config: config})
		if err != nil {
			t.Fatal(err)
		}
		requireNoErrors(t, response.Diagnostics)
		if response.Result == nil || len(response.Private) == 0 {
			t.Fatal("missing result or private token")
		}
		private = append(private, response.Private)
	}
	if len(resource.connections) != 2 {
		t.Fatal("connections were not independently registered")
	}
	for range 2 {
		response, err := server.CloseEphemeralResource(context.Background(), &tfprotov6.CloseEphemeralResourceRequest{TypeName: "sshconnection_connection", Private: private[0]})
		if err != nil {
			t.Fatal(err)
		}
		requireNoErrors(t, response.Diagnostics)
	}
	if connections[0].closes != 1 || connections[1].closes != 0 || len(resource.connections) != 1 {
		t.Fatal("close affected the wrong connection or ran twice")
	}
	response, err := server.CloseEphemeralResource(context.Background(), &tfprotov6.CloseEphemeralResourceRequest{TypeName: "sshconnection_connection", Private: private[1]})
	if err != nil {
		t.Fatal(err)
	}
	requireNoErrors(t, response.Diagnostics)
	if connections[1].closes != 1 || len(resource.connections) != 0 {
		t.Fatal("connection leaked")
	}
}

func TestOpenFailuresCleanUp(t *testing.T) {
	for _, scenario := range []string{"startup", "readiness", "cancel", "cancel_after_ready", "close_error"} {
		t.Run(scenario, func(t *testing.T) {
			ctx, cancel := context.WithCancel(context.Background())
			defer cancel()
			conn := &fakeConnection{wait: func(readyCtx context.Context) error {
				deadline, ok := readyCtx.Deadline()
				if !ok || time.Until(deadline) > startupTimeout {
					t.Error("startup timeout missing")
				}
				switch scenario {
				case "readiness", "close_error":
					return errors.New("forward denied")
				case "cancel":
					cancel()
					return readyCtx.Err()
				case "cancel_after_ready":
					cancel()
					return nil
				}
				return nil
			}}
			if scenario == "close_error" {
				conn.closeErr = errors.New("log write failed")
			}
			resource := NewSSHConnection().(*sshConnectionResource)
			resource.start = func(context.Context, connection.Model) (connection.Connection, error) {
				if scenario == "startup" {
					return nil, errors.New("SSH unavailable")
				}
				return conn, nil
			}
			server, config := testServer(t, resource)
			response, err := server.OpenEphemeralResource(ctx, &tfprotov6.OpenEphemeralResourceRequest{TypeName: "sshconnection_connection", Config: config})
			if err != nil {
				t.Fatal(err)
			}
			if diagnosticText(response.Diagnostics) == "" {
				t.Fatal("failure not reported")
			}
			if len(resource.connections) != 0 {
				t.Fatal("failed connection registered")
			}
			expectedCloses := 1
			if scenario == "startup" {
				expectedCloses = 0
			}
			if conn.closes != expectedCloses {
				t.Fatalf("closed %d times", conn.closes)
			}
			if scenario == "close_error" && !strings.Contains(diagnosticText(response.Diagnostics), "log write failed") {
				t.Fatal(response.Diagnostics)
			}
		})
	}
}

func TestPrivateTokenValidation(t *testing.T) {
	resource := NewSSHConnection().(*sshConnectionResource)
	server, _ := testServer(t, resource)
	for _, scenario := range []struct {
		token     json.RawMessage
		wantError bool
	}{
		{nil, false}, {json.RawMessage(`"unknown-token"`), false},
		{json.RawMessage(`123`), true}, {json.RawMessage(`null`), true}, {json.RawMessage(`""`), true},
	} {
		private := map[string][]byte{}
		if scenario.token != nil {
			private[connectionKey] = scenario.token
		}
		bytes, err := json.Marshal(private)
		if err != nil {
			t.Fatal(err)
		}
		response, err := server.CloseEphemeralResource(context.Background(), &tfprotov6.CloseEphemeralResourceRequest{TypeName: "sshconnection_connection", Private: bytes})
		if err != nil {
			t.Fatal(err)
		}
		if got := diagnosticText(response.Diagnostics) != ""; got != scenario.wantError {
			t.Fatalf("token %s: %v", scenario.token, response.Diagnostics)
		}
	}
}

func TestConcurrentClose(t *testing.T) {
	resource := NewSSHConnection().(*sshConnectionResource)
	conn := &fakeConnection{}
	resource.start = func(context.Context, connection.Model) (connection.Connection, error) { return conn, nil }
	server, config := testServer(t, resource)
	opened, err := server.OpenEphemeralResource(context.Background(), &tfprotov6.OpenEphemeralResourceRequest{TypeName: "sshconnection_connection", Config: config})
	if err != nil {
		t.Fatal(err)
	}
	requireNoErrors(t, opened.Diagnostics)
	var callers sync.WaitGroup
	for range 4 {
		callers.Go(func() {
			response, err := server.CloseEphemeralResource(context.Background(), &tfprotov6.CloseEphemeralResourceRequest{TypeName: "sshconnection_connection", Private: opened.Private})
			if err != nil {
				t.Error(err)
				return
			}
			for _, diagnostic := range response.Diagnostics {
				if diagnostic.Severity == tfprotov6.DiagnosticSeverityError {
					t.Error(diagnostic)
				}
			}
		})
	}
	callers.Wait()
	if conn.closes != 1 {
		t.Fatalf("closed %d times", conn.closes)
	}
}

func TestFactorySharesResource(t *testing.T) {
	provider := New("test")().(*sshConnectionProvider)
	factory := provider.EphemeralResources(context.Background())[0]
	if factory() != factory() {
		t.Fatal("RPC factories lost shared registry")
	}
	other := New("test")().(*sshConnectionProvider).EphemeralResources(context.Background())[0]
	if factory() == other() {
		t.Fatal("different providers share connections")
	}
}

func TestTerraformValidationRejectsConfiguration(t *testing.T) {
	resource := NewSSHConnection().(*sshConnectionResource)
	resource.start = func(context.Context, connection.Model) (connection.Connection, error) {
		t.Fatal("invalid configuration started SSH")
		return nil, nil
	}
	server, original := testServer(t, resource)
	schema, err := server.GetProviderSchema(context.Background(), &tfprotov6.GetProviderSchemaRequest{})
	if err != nil {
		t.Fatal(err)
	}
	objectType := schema.EphemeralResourceSchemas["sshconnection_connection"].ValueType().(tftypes.Object)
	decoded, err := original.Unmarshal(objectType)
	if err != nil {
		t.Fatal(err)
	}
	var values map[string]tftypes.Value
	if err := decoded.As(&values); err != nil {
		t.Fatal(err)
	}
	values["config_file"] = tftypes.NewValue(tftypes.String, "/tmp/ssh-config")
	invalid, err := tfprotov6.NewDynamicValue(objectType, tftypes.NewValue(objectType, values))
	if err != nil {
		t.Fatal(err)
	}
	validation, err := server.ValidateEphemeralResourceConfig(context.Background(), &tfprotov6.ValidateEphemeralResourceConfigRequest{TypeName: "sshconnection_connection", Config: &invalid})
	if err != nil {
		t.Fatal(err)
	}
	if !strings.Contains(diagnosticText(validation.Diagnostics), "External SSH configuration is disabled") {
		t.Fatal(validation.Diagnostics)
	}
	opened, err := server.OpenEphemeralResource(context.Background(), &tfprotov6.OpenEphemeralResourceRequest{TypeName: "sshconnection_connection", Config: &invalid})
	if err != nil {
		t.Fatal(err)
	}
	if !strings.Contains(diagnosticText(opened.Diagnostics), "External SSH configuration is disabled") {
		t.Fatal(opened.Diagnostics)
	}
}
