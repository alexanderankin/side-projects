package connection

import (
	"context"
	"fmt"
	"os"
	"os/exec"
	"strings"
	"sync"

	"github.com/hashicorp/terraform-plugin-framework/types"
)

// StartCLI is the only backend-specific entry point. CLI arguments never appear
// on Connection, so users of a running connection need not know its transport.
func StartCLI(ctx context.Context, model Model) (Connection, error) {
	executable, err := os.Executable()
	if err != nil {
		return nil, err
	}
	return startCLI(ctx, executable, model)
}

func startCLI(ctx context.Context, executable string, model Model) (Connection, error) {
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	// Preserve the requested log destination, but keep readiness diagnostics on
	// the supervised stderr stream. Quiet mode suppresses user-facing logging,
	// not the backend's internal readiness protocol.
	logPath := model.LogFile.ValueString()
	model.LogFile = types.StringNull()
	model.Quiet = types.BoolValue(false)
	args, diags := buildSSHArgs(ctx, model)
	if diags.HasError() {
		var messages []string
		for _, d := range diags {
			messages = append(messages, d.Summary()+": "+d.Detail())
		}
		return nil, fmt.Errorf("invalid SSH configuration: %s", strings.Join(messages, "; "))
	}
	// Own these options: readiness requires diagnostics from this process, and
	// lifetime ownership requires that SSH neither daemonize nor reuse a master.
	managed := []string{"-o", "LogLevel=DEBUG1", "-o", "ExitOnForwardFailure=yes",
		"-o", "ForkAfterAuthentication=no", "-o", "ControlMaster=no",
		"-o", "ControlPath=none", "-o", "ControlPersist=no", "-o", "BatchMode=yes"}
	managed = append(managed, args...)
	c := &cliConnection{done: make(chan struct{}), diagnostics: &startupLog{ready: make(chan struct{})}}
	var err error
	if logPath != "" {
		c.logFile, err = os.OpenFile(logPath, os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0600)
		if err != nil {
			return nil, err
		}
		c.diagnostics.file = c.logFile
	}
	reader, writer, err := os.Pipe()
	if err != nil {
		if c.logFile != nil {
			_ = c.logFile.Close()
		}
		return nil, err
	}
	c.keepalive = writer
	cmd := exec.Command(executable, append([]string{"--ssh-supervisor"}, managed...)...)
	cmd.Stdin = reader
	cmd.Stderr = c.diagnostics
	// No remote command is run, so stdout is discarded, not accumulated.
	err = cmd.Start()
	_ = reader.Close()
	if err != nil {
		_ = writer.Close()
		if c.logFile != nil {
			_ = c.logFile.Close()
		}
		return nil, err
	}
	go func() {
		c.waitErr = cmd.Wait()
		close(c.done)
	}()
	return c, nil
}

type cliConnection struct {
	keepalive   *os.File
	logFile     *os.File
	diagnostics *startupLog
	done        chan struct{}
	waitErr     error // Written before done closes; read only after it closes.
	closeOnce   sync.Once
	closeErr    error
}

func (c *cliConnection) WaitReady(ctx context.Context) error {
	select {
	case <-ctx.Done():
		return fmt.Errorf("waiting for SSH readiness: %w\n%s", ctx.Err(), c.diagnostics.tail())
	case <-c.done:
		return c.exited()
	case <-c.diagnostics.ready:
		select {
		case <-c.done:
			return c.exited()
		default:
			return ctx.Err()
		}
	}
}

func (c *cliConnection) exited() error {
	return fmt.Errorf("SSH exited before readiness (%v)\n%s", c.waitErr, c.diagnostics.tail())
}

func (c *cliConnection) Close() error {
	c.closeOnce.Do(func() {
		c.closeErr = c.keepalive.Close()
		<-c.done
		if c.logFile != nil {
			_ = c.logFile.Close()
		}
	})
	return c.closeErr
}

// OpenSSH sets up local listeners before entering client_loop. Remote forwarding
// is asynchronous: ssh_init_forwarding announces pending replies before that
// loop, and forwarding_success reports when every reply has arrived. Watching
// both events also handles forwards configured in ~/.ssh/config.
// See openssh-portable ssh.c (ssh_init_forwarding, forwarding_success) and
// clientloop.c (client_loop). Unknown log formats fail closed via the timeout.
type startupLog struct {
	mu                                        sync.Mutex
	ready                                     chan struct{}
	file                                      *os.File
	partial                                   string
	recent                                    string
	interactive, pending, confirmed, signaled bool
}

func (s *startupLog) Write(p []byte) (int, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	if s.file != nil {
		if _, err := s.file.Write(p); err != nil {
			return 0, err
		}
	}
	s.recent += string(p)
	if len(s.recent) > 16384 {
		s.recent = s.recent[len(s.recent)-16384:]
	}
	s.partial += string(p)
	for {
		i := strings.IndexByte(s.partial, '\n')
		if i < 0 {
			break
		}
		line := strings.TrimSuffix(s.partial[:i], "\r")
		s.partial = s.partial[i+1:]
		switch {
		case line == "debug1: Entering interactive session.":
			s.interactive = true
		case strings.HasPrefix(line, "debug1: ssh_init_forwarding: expecting replies for "):
			s.pending = true
		case line == "debug1: forwarding_success: all expected forwarding replies received":
			s.confirmed = true
		}
		if s.interactive && (!s.pending || s.confirmed) && !s.signaled {
			s.signaled = true
			close(s.ready)
		}
	}
	if len(s.partial) > 16384 {
		s.partial = s.partial[len(s.partial)-16384:]
	}
	return len(p), nil
}

func (s *startupLog) tail() string {
	s.mu.Lock()
	defer s.mu.Unlock()
	return strings.TrimSpace(s.recent)
}
