// Package connection defines the backend-independent lifetime of an SSH connection.
package connection

import "context"

// Connection is already started, but may still be authenticating or binding
// listeners. A future native backend can implement this without a subprocess.
type Connection interface {
	// WaitReady waits for authentication, local listener setup, and server
	// acknowledgement of remote forwards. It does not probe target services.
	// Cancellation only cancels the wait; the owner must still call Close.
	WaitReady(context.Context) error
	// Close terminates the connection and waits for cleanup. It is idempotent.
	Close() error
}
