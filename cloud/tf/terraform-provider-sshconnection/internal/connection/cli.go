package connection

import (
	"context"
	"errors"
	"fmt"
	"os"
	"os/exec"
	"strings"
	"sync"
)

// StartSupervisor starts a second copy of this provider in supervisor mode.
// The supervisor runs SSH and stops it if the provider disappears.
func StartSupervisor(ctx context.Context, model Model) (Connection, error) {
	executable, err := os.Executable()
	if err != nil {
		return nil, err
	}
	return startSupervisor(ctx, executable, model)
}

func startSupervisor(ctx context.Context, executable string, model Model) (Connection, error) {
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	args, diagnostics := buildSSHArgs(ctx, model)
	if diagnostics.HasError() {
		var messages []string
		for _, diagnostic := range diagnostics {
			messages = append(messages, diagnostic.Summary()+": "+diagnostic.Detail())
		}
		return nil, fmt.Errorf("invalid SSH configuration: %s", strings.Join(messages, "; "))
	}
	connection := &cliConnection{supervisorExited: make(chan struct{}), diagnostics: &startupLog{ready: make(chan struct{})}}
	// Until command.Start succeeds, this function owns every opened file.
	started := false
	defer func() {
		if !started {
			if connection.keepalive != nil {
				_ = connection.keepalive.Close()
			}
			if connection.logFile != nil {
				_ = connection.logFile.Close()
			}
		}
	}()
	var err error
	logPath := model.LogFile.ValueString()
	if logPath != "" {
		connection.logFile, err = os.OpenFile(logPath, os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0600)
		if err != nil {
			return nil, fmt.Errorf("open SSH log file: %w", err)
		}
		connection.diagnostics.file = connection.logFile
	}
	reader, writer, err := os.Pipe()
	if err != nil {
		return nil, fmt.Errorf("create supervisor keepalive pipe: %w", err)
	}
	// The supervisor inherits the reader. The provider holds the writer open;
	// closing it (including on provider exit) tells the supervisor to stop SSH.
	defer func() { _ = reader.Close() }()
	connection.keepalive = writer
	command := exec.Command(executable, append([]string{"--ssh-supervisor"}, args...)...)
	command.Stdin = reader
	command.Stderr = connection.diagnostics
	// No remote command is run, so stdout is discarded, not accumulated.
	err = command.Start()
	if err != nil {
		return nil, fmt.Errorf("start SSH supervisor: %w", err)
	}
	started = true
	// Wait collects the process exit status and finishes copying stderr. Only
	// then do we announce completion; readers can safely access waitErr afterward.
	go func() {
		connection.waitErr = command.Wait()
		close(connection.supervisorExited)
	}()
	return connection, nil
}

type cliConnection struct {
	keepalive        *os.File
	logFile          *os.File
	diagnostics      *startupLog
	supervisorExited chan struct{}
	waitErr          error // Written before supervisorExited closes; read only afterward.
	closeOnce        sync.Once
	closeErr         error
}

func (connection *cliConnection) WaitReady(ctx context.Context) error {
	select {
	case <-ctx.Done():
		return fmt.Errorf("waiting for SSH readiness: %w\n%s", ctx.Err(), connection.diagnostics.tail())
	case <-connection.supervisorExited:
		return connection.exited()
	case <-connection.diagnostics.ready:
		select {
		case <-connection.supervisorExited:
			return connection.exited()
		default:
			return ctx.Err()
		}
	}
}

func (connection *cliConnection) exited() error {
	if connection.waitErr == nil {
		return fmt.Errorf("SSH exited before readiness without reporting an error\n%s", connection.diagnostics.tail())
	}
	return fmt.Errorf("SSH exited before readiness: %w\n%s", connection.waitErr, connection.diagnostics.tail())
}

func (connection *cliConnection) Close() error {
	// sync.Once makes concurrent or repeated Close calls share one cleanup.
	connection.closeOnce.Do(func() {
		connection.closeErr = connection.keepalive.Close()
		<-connection.supervisorExited
		if err := connection.diagnostics.copyError(); err != nil {
			connection.closeErr = errors.Join(connection.closeErr, fmt.Errorf("copy SSH log: %w", err))
		}
		if connection.logFile != nil {
			connection.closeErr = errors.Join(connection.closeErr, connection.logFile.Close())
		}
	})
	return connection.closeErr
}
