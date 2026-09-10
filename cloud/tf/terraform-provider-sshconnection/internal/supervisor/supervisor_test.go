//go:build unix

package supervisor

import (
	"errors"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"syscall"
	"testing"
	"time"
)

func TestRunKillsSSHWhenKeepaliveCloses(t *testing.T) {
	tempDir := t.TempDir()
	pidFile := filepath.Join(tempDir, "ssh.pid")
	fakeSSH := filepath.Join(tempDir, "ssh")
	if err := os.WriteFile(fakeSSH, []byte("#!/bin/sh\nprintf '%s\\n' \"$$\" > \"$SSH_PID_FILE\"\nwhile :; do :; done\n"), 0o755); err != nil {
		t.Fatal(err)
	}
	t.Setenv("PATH", tempDir+string(os.PathListSeparator)+os.Getenv("PATH"))
	t.Setenv("SSH_PID_FILE", pidFile)

	reader, writer, err := os.Pipe()
	if err != nil {
		t.Fatal(err)
	}
	done := make(chan error, 1)
	go func() { done <- Run(reader, []string{"ignored"}) }()

	var pid int
	deadline := time.Now().Add(5 * time.Second)
	for time.Now().Before(deadline) {
		raw, readErr := os.ReadFile(pidFile)
		if readErr == nil {
			pid, err = strconv.Atoi(strings.TrimSpace(string(raw)))
			if err != nil {
				t.Fatal(err)
			}
			break
		}
		time.Sleep(10 * time.Millisecond)
	}
	if pid == 0 {
		t.Fatal("fake SSH process did not start")
	}

	if err := writer.Close(); err != nil {
		t.Fatal(err)
	}
	select {
	case err := <-done:
		if err != nil {
			t.Fatalf("supervisor returned an error: %v", err)
		}
	case <-time.After(5 * time.Second):
		t.Fatal("supervisor did not stop after keepalive closed")
	}
	_ = reader.Close()

	if err := syscall.Kill(pid, 0); !errors.Is(err, syscall.ESRCH) {
		t.Fatalf("SSH process %d is still running", pid)
	}
}
