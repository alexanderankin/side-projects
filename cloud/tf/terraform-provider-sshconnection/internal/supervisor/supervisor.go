package supervisor

import (
	"errors"
	"io"
	"os"
	"os/exec"
)

// Run starts SSH and keeps it alive only while keepalive remains open. The
// provider owns the other end of keepalive, so provider termination always
// causes the SSH process to be killed and reaped by this supervisor.
func Run(keepalive io.Reader, args []string) error {
	cmd := exec.Command("ssh", args...)
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	if err := cmd.Start(); err != nil {
		return err
	}

	done := make(chan error, 1)
	go func() { done <- cmd.Wait() }()
	keepaliveClosed := make(chan struct{})
	go func() {
		_, _ = io.Copy(io.Discard, keepalive)
		close(keepaliveClosed)
	}()

	select {
	case err := <-done:
		return err
	case <-keepaliveClosed:
		if err := cmd.Process.Kill(); err != nil && !errors.Is(err, os.ErrProcessDone) {
			return err
		}
		<-done
		return nil
	}
}
