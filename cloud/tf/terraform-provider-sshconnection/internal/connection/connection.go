// Package connection starts SSH, waits for forwarding setup, and stops it.
package connection

import "context"

// Connection is already started, but may still be authenticating or binding
// listeners. The provider uses this small interface to manage its lifetime.
type Connection interface {
	// WaitReady waits for authentication, local listener setup, and server
	// acknowledgement of remote forwards. It does not probe target services.
	// Cancellation only cancels the wait; the owner must still call Close.
	WaitReady(context.Context) error
	// Close terminates the connection and waits for cleanup. It is idempotent.
	Close() error
}
