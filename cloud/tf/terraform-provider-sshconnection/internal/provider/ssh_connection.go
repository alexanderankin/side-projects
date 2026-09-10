package provider

import (
	"context"
	"crypto/rand"
	"encoding/hex"
	"encoding/json"
	"sync"
	"time"

	"github.com/toor/terraform-provider-ssh-connection/internal/connection"

	"github.com/hashicorp/terraform-plugin-framework/ephemeral"
)

// Keep this private key for compatibility. Its value is a random token, not a PID.
const connectionKey = "ssh_connection_provider_process_id"
const startupTimeout = 60 * time.Second

var _ ephemeral.EphemeralResource = (*sshConnectionResource)(nil)
var _ ephemeral.EphemeralResourceWithValidateConfig = (*sshConnectionResource)(nil)
var _ ephemeral.EphemeralResourceWithClose = (*sshConnectionResource)(nil)

type sshConnectionResource struct {
	// Terraform can open/close different instances concurrently. Protect only
	// the token lookup; never hold this lock while waiting for SSH to stop.
	mu          sync.Mutex
	connections map[string]connection.Connection
	start       func(context.Context, connection.Model) (connection.Connection, error)
}

func NewSSHConnection() ephemeral.EphemeralResource {
	return &sshConnectionResource{connections: make(map[string]connection.Connection), start: connection.StartSupervisor}
}

func (resource *sshConnectionResource) Metadata(_ context.Context, request ephemeral.MetadataRequest, response *ephemeral.MetadataResponse) {
	response.TypeName = request.ProviderTypeName + "_connection"
}

func (resource *sshConnectionResource) Open(ctx context.Context, request ephemeral.OpenRequest, response *ephemeral.OpenResponse) {
	var model connection.Model
	response.Diagnostics.Append(request.Config.Get(ctx, &model)...)
	if response.Diagnostics.HasError() {
		return
	}

	response.Diagnostics.Append(connection.Validate(ctx, model)...)
	if response.Diagnostics.HasError() {
		return
	}

	// Private data survives from Open to Close, unlike this method's local variables.
	tokenBytes := make([]byte, 16)
	_, err := rand.Read(tokenBytes)
	if err != nil {
		response.Diagnostics.AddError("Unable to track SSH connection", err.Error())
		return
	}
	token := hex.EncodeToString(tokenBytes)
	encodedToken, err := json.Marshal(token)
	if err != nil {
		response.Diagnostics.AddError("Unable to track SSH connection", err.Error())
		return
	}
	response.Diagnostics.Append(response.Private.SetKey(ctx, connectionKey, encodedToken)...)
	if response.Diagnostics.HasError() {
		return
	}

	runningConnection, err := resource.start(ctx, model)
	if err != nil {
		response.Diagnostics.AddError("Unable to start SSH", err.Error())
		return
	}
	// Until registration succeeds, any return path must stop the new connection.
	registered := false
	defer func() {
		if !registered {
			err := runningConnection.Close()
			if err != nil {
				response.Diagnostics.AddError("Unable to stop SSH", err.Error())
			}
		}
	}()
	readyCtx, cancel := context.WithTimeout(ctx, startupTimeout)
	defer cancel()
	err = runningConnection.WaitReady(readyCtx)
	if err != nil {
		response.Diagnostics.AddError("SSH connection did not become ready", err.Error())
		return
	}

	resource.mu.Lock()
	defer resource.mu.Unlock()
	// A cancellation can arrive just as WaitReady succeeds.
	err = readyCtx.Err()
	if err != nil {
		response.Diagnostics.AddError("SSH connection opening was canceled", err.Error())
		return
	}
	resource.connections[token] = runningConnection
	registered = true
}

func (resource *sshConnectionResource) Close(ctx context.Context, request ephemeral.CloseRequest, response *ephemeral.CloseResponse) {
	raw, diagnostics := request.Private.GetKey(ctx, connectionKey)
	response.Diagnostics.Append(diagnostics...)
	if response.Diagnostics.HasError() || len(raw) == 0 {
		return
	}
	var token string
	err := json.Unmarshal(raw, &token)
	if err != nil {
		response.Diagnostics.AddError("Unable to identify SSH connection", err.Error())
		return
	}
	if token == "" {
		response.Diagnostics.AddError("Unable to identify SSH connection", "The private connection token is empty or null.")
		return
	}
	resource.mu.Lock()
	runningConnection, ok := resource.connections[token]
	delete(resource.connections, token)
	resource.mu.Unlock()
	if !ok {
		return
	}
	err = runningConnection.Close()
	if err != nil {
		response.Diagnostics.AddError("Unable to stop SSH", err.Error())
	}
}

// ValidateConfig runs during planning too, when some expressions are unknown.
func (resource *sshConnectionResource) ValidateConfig(ctx context.Context, request ephemeral.ValidateConfigRequest, response *ephemeral.ValidateConfigResponse) {
	var model connection.Model
	response.Diagnostics.Append(request.Config.Get(ctx, &model)...)
	if response.Diagnostics.HasError() {
		return
	}
	response.Diagnostics.Append(connection.Validate(ctx, model)...)
}
