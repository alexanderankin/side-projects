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
	command := exec.Command("ssh", args...)
	command.Stdout = os.Stdout
	command.Stderr = os.Stderr
	if err := command.Start(); err != nil {
		return err
	}

	sshExited := make(chan error, 1)
	// A goroutine waits in the background while the main function watches the pipe.
	go func() { sshExited <- command.Wait() }()

	providerDisconnected := make(chan struct{})
	go func() {
		_, _ = io.Copy(io.Discard, keepalive)
		close(providerDisconnected)
	}()

	// Whichever event happens first decides how to finish. Killing SSH is not
	// enough: receiving from sshExited also waits for the OS to collect it.
	select {
	case err := <-sshExited:
		return err
	case <-providerDisconnected:
		if err := command.Process.Kill(); err != nil && !errors.Is(err, os.ErrProcessDone) {
			return err
		}
		<-sshExited
		return nil
	}
}
