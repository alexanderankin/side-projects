package connection

import (
	"io"
	"strings"
	"sync"
)

const (
	diagnosticLimit        = 16 * 1024
	clientLoopStarted      = "debug1: Entering interactive session."
	forwardRepliesPending  = "debug1: ssh_init_forwarding: expecting replies for "
	forwardRepliesReceived = "debug1: forwarding_success: all expected forwarding replies received"
)

// startupLog receives the supervised SSH stderr stream. OpenSSH creates local
// listeners before client_loop, and announces remote replies in
// ssh_init_forwarding and forwarding_success. See ../how_it_works.md for sources.
type startupLog struct {
	// Write runs while WaitReady may read diagnostics. The mutex prevents them
	// from changing or reading these fields at the same time.
	mu                     sync.Mutex
	ready                  chan struct{}
	file                   io.Writer
	fileError              error
	partialLine            string
	discardingLongLine     bool
	recent                 string
	clientLoopStarted      bool
	forwardRepliesPending  bool
	forwardRepliesReceived bool
	readySignaled          bool
}

func (log *startupLog) Write(bytes []byte) (int, error) {
	log.mu.Lock()
	defer log.mu.Unlock()

	text := string(bytes)
	log.recent += text
	if len(log.recent) > diagnosticLimit {
		log.recent = log.recent[len(log.recent)-diagnosticLimit:]
	}
	log.consumeLines(text)

	if log.file != nil && log.fileError == nil {
		written, err := log.file.Write(bytes)
		if err == nil && written != len(bytes) {
			err = io.ErrShortWrite
		}
		log.fileError = err
	}
	// A failed optional log copy must not stop os/exec from draining stderr.
	// Close reports the saved file error after SSH has been stopped.
	return len(bytes), nil
}

// consumeLines handles arbitrary write boundaries. An oversized line is ignored
// until its newline, so its suffix cannot be mistaken for a readiness message.
func (log *startupLog) consumeLines(text string) {
	for len(text) > 0 {
		newline := strings.IndexByte(text, '\n')
		fragment := text
		if newline >= 0 {
			fragment = text[:newline]
		}
		if !log.discardingLongLine {
			if len(log.partialLine)+len(fragment) > diagnosticLimit {
				log.partialLine = ""
				log.discardingLongLine = true
			} else {
				log.partialLine += fragment
			}
		}
		if newline < 0 {
			return
		}
		if !log.discardingLongLine {
			log.observeLine(strings.TrimSuffix(log.partialLine, "\r"))
		}
		log.partialLine = ""
		log.discardingLongLine = false
		text = text[newline+1:]
	}
}

func (log *startupLog) observeLine(line string) {
	switch {
	case line == clientLoopStarted:
		log.clientLoopStarted = true
	case strings.HasPrefix(line, forwardRepliesPending):
		log.forwardRepliesPending = true
	case line == forwardRepliesReceived:
		log.forwardRepliesReceived = true
	}
	if log.clientLoopStarted && (!log.forwardRepliesPending || log.forwardRepliesReceived) && !log.readySignaled {
		log.readySignaled = true
		close(log.ready) // Closing a channel wakes every WaitReady caller.
	}
}

func (log *startupLog) tail() string {
	log.mu.Lock()
	defer log.mu.Unlock()
	return strings.TrimSpace(log.recent)
}

func (log *startupLog) copyError() error {
	log.mu.Lock()
	defer log.mu.Unlock()
	return log.fileError
}
