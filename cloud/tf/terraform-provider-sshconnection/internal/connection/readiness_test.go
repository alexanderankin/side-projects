package connection

import (
	"context"
	"errors"
	"io"
	"os"
	"strings"
	"sync"
	"testing"
	"time"
)

func isReady(log *startupLog) bool {
	select {
	case <-log.ready:
		return true
	default:
		return false
	}
}

func TestReadinessMessages(t *testing.T) {
	log := &startupLog{ready: make(chan struct{})}
	for _, part := range []string{forwardRepliesPending + "2 forwards\r\n", "debug1: Entering ", "interactive session.\r", "\n"} {
		_, _ = log.Write([]byte(part))
		if isReady(log) {
			t.Fatal("ready before remote replies")
		}
	}
	_, _ = log.Write([]byte(forwardRepliesReceived + "\n"))
	if !isReady(log) {
		t.Fatal("not ready after replies")
	}
	// Repeating a message must not close the channel twice (which panics in Go).
	_, _ = log.Write([]byte(clientLoopStarted + "\n" + forwardRepliesReceived + "\n"))
}

func TestReadinessOversizedLine(t *testing.T) {
	log := &startupLog{ready: make(chan struct{})}
	_, _ = log.Write([]byte(strings.Repeat("x", diagnosticLimit+1)))
	_, _ = log.Write([]byte(clientLoopStarted + "\n"))
	if isReady(log) {
		t.Fatal("suffix of oversized line was treated as a message")
	}
	if len(log.tail()) > diagnosticLimit {
		t.Fatal("diagnostics exceeded limit")
	}
	_, _ = log.Write([]byte(clientLoopStarted + "\n"))
	if !isReady(log) {
		t.Fatal("did not resume after oversized line")
	}
}

type failingLogWriter struct {
	calls int
	short bool
}

func (writer *failingLogWriter) Write(p []byte) (int, error) {
	writer.calls++
	if writer.short {
		return len(p) - 1, nil
	}
	return 0, errors.New("disk full")
}

func TestLogCopyFailureDoesNotStopReadiness(t *testing.T) {
	for _, short := range []bool{false, true} {
		writer := &failingLogWriter{short: short}
		log := &startupLog{ready: make(chan struct{}), file: writer}
		_, _ = log.Write([]byte("startup\n"))
		message := []byte(clientLoopStarted + "\n")
		n, err := log.Write(message)
		if err != nil || n != len(message) || !isReady(log) {
			t.Fatalf("readiness lost on write failure: %d %v", n, err)
		}
		if writer.calls != 1 || log.copyError() == nil {
			t.Fatal("failed copy was retried or forgotten")
		}
		if short && !errors.Is(log.copyError(), io.ErrShortWrite) {
			t.Fatal(log.copyError())
		}
		reader, pipeWriter, err := os.Pipe()
		if err != nil {
			t.Fatal(err)
		}
		defer reader.Close()
		exited := make(chan struct{})
		close(exited)
		conn := &cliConnection{keepalive: pipeWriter, diagnostics: log, supervisorExited: exited}
		// Concurrent closes must all report the same saved error without races.
		var callers sync.WaitGroup
		for range 4 {
			callers.Go(func() {
				if err := conn.Close(); !errors.Is(err, log.copyError()) {
					t.Errorf("close lost copy error: %v", err)
				}
			})
		}
		callers.Wait()
	}
}

func TestWaitReadyFailure(t *testing.T) {
	for _, exitErr := range []error{nil, errors.New("exit status 1")} {
		exited := make(chan struct{})
		close(exited)
		conn := &cliConnection{diagnostics: &startupLog{ready: make(chan struct{})}, supervisorExited: exited, waitErr: exitErr}
		err := conn.WaitReady(context.Background())
		if err == nil || strings.Contains(err.Error(), "<nil>") {
			t.Fatal(err)
		}
	}
	log := &startupLog{ready: make(chan struct{})}
	_, _ = log.Write([]byte("an unknown SSH message\n"))
	conn := &cliConnection{diagnostics: log, supervisorExited: make(chan struct{})}
	ctx, cancel := context.WithTimeout(context.Background(), time.Millisecond)
	defer cancel()
	if err := conn.WaitReady(ctx); !errors.Is(err, context.DeadlineExceeded) || !strings.Contains(err.Error(), "unknown SSH message") {
		t.Fatal(err)
	}
}
