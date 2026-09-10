//go:build unix

package supervisor

import (
	"errors"
	"io"
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
	defer func() { _ = reader.Close() }()
	defer func() { _ = writer.Close() }()
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

func TestRunStartupFailure(t *testing.T) {
	t.Setenv("PATH", t.TempDir())
	if err := Run(strings.NewReader(""), nil); err == nil {
		t.Fatal("missing SSH did not fail")
	}
}

func TestRunNaturalExit(t *testing.T) {
	for _, status := range []string{"0", "7"} {
		t.Run(status, func(t *testing.T) {
			dir := t.TempDir()
			if err := os.WriteFile(filepath.Join(dir, "ssh"), []byte("#!/bin/sh\nexit "+status+"\n"), 0755); err != nil {
				t.Fatal(err)
			}
			t.Setenv("PATH", dir)
			reader, writer := io.Pipe()
			defer func() { _ = reader.Close() }()
			defer func() { _ = writer.Close() }()
			done := make(chan error, 1)
			go func() { done <- Run(reader, nil) }()
			select {
			case err := <-done:
				if (err != nil) != (status != "0") {
					t.Fatalf("status %s: %v", status, err)
				}
			case <-time.After(5 * time.Second):
				t.Fatal("supervisor waited for pipe after SSH exited")
			}
		})
	}
}
