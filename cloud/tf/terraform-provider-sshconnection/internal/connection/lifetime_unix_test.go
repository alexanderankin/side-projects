//go:build unix

package connection

import (
	"context"
	"errors"
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"strings"
	"syscall"
	"testing"
	"time"

	"github.com/hashicorp/terraform-plugin-framework/types"
)

// This subprocess acts as the provider. Exiting without Close must still stop
// SSH: the OS closes our pipe writer, and the separate supervisor notices EOF.
func TestProviderExitHelper(t *testing.T) {
	if os.Getenv("SSH_TEST_EXIT_HELPER") != "1" {
		return
	}
	conn, err := startSupervisor(context.Background(), os.Getenv("SSH_TEST_PROVIDER"), Model{Destination: types.StringValue("example.invalid")})
	if err != nil {
		t.Fatal(err)
	}
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	if err := conn.WaitReady(ctx); err != nil {
		t.Fatal(err)
	}
	os.Exit(0)
}

func TestProviderExitStopsSSH(t *testing.T) {
	directory := t.TempDir()
	provider := filepath.Join(directory, "provider")
	if output, err := exec.Command("go", "build", "-o", provider, "../..").CombinedOutput(); err != nil {
		t.Fatalf("build: %v\n%s", err, output)
	}
	pidFile := filepath.Join(directory, "ssh.pid")
	script := "#!/bin/sh\nprintf '%s\\n' \"$$\" > \"$SSH_TEST_PID_FILE\"\nprintf 'debug1: Entering interactive session.\\n' >&2\nexec /bin/sleep 60\n"
	if err := os.WriteFile(filepath.Join(directory, "ssh"), []byte(script), 0755); err != nil {
		t.Fatal(err)
	}
	t.Setenv("PATH", directory+string(os.PathListSeparator)+os.Getenv("PATH"))
	t.Setenv("SSH_TEST_EXIT_HELPER", "1")
	t.Setenv("SSH_TEST_PROVIDER", provider)
	t.Setenv("SSH_TEST_PID_FILE", pidFile)
	testExecutable, err := os.Executable()
	if err != nil {
		t.Fatal(err)
	}
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	helper := exec.CommandContext(ctx, testExecutable, "-test.run=^TestProviderExitHelper$")
	// Bound output-pipe cleanup too, in case a broken supervisor inherits stderr.
	helper.WaitDelay = time.Second
	if output, err := helper.CombinedOutput(); err != nil {
		t.Fatalf("provider helper: %v\n%s", err, output)
	}
	raw, err := os.ReadFile(pidFile)
	if err != nil {
		t.Fatal(err)
	}
	pid, err := strconv.Atoi(strings.TrimSpace(string(raw)))
	if err != nil {
		t.Fatal(err)
	}
	for deadline := time.Now().Add(5 * time.Second); time.Now().Before(deadline); {
		if errors.Is(syscall.Kill(pid, 0), syscall.ESRCH) {
			return
		}
		time.Sleep(10 * time.Millisecond)
	}
	_ = syscall.Kill(pid, syscall.SIGKILL)
	t.Fatalf("SSH process %d survived provider exit", pid)
}

func TestCLIStartupFailures(t *testing.T) {
	model := Model{Destination: types.StringValue("example.invalid")}
	ctx, cancel := context.WithCancel(context.Background())
	cancel()
	if _, err := startSupervisor(ctx, "unused", model); !errors.Is(err, context.Canceled) {
		t.Fatal(err)
	}
	missing := filepath.Join(t.TempDir(), "missing")
	model.LogFile = types.StringValue(t.TempDir()) // A directory cannot be a log file.
	if _, err := startSupervisor(context.Background(), missing, model); err == nil || !strings.Contains(err.Error(), "open SSH log file") {
		t.Fatal(err)
	}
	model.LogFile = types.StringValue(filepath.Join(t.TempDir(), "ssh.log"))
	if _, err := startSupervisor(context.Background(), missing, model); err == nil || !strings.Contains(err.Error(), "start SSH supervisor") {
		t.Fatal(err)
	}
}
