package slf4go_binding_logrus_test

import (
	"gos-test-simple/demo/slf4go"
	_ "gos-test-simple/demo/slf4go_binding_logrus"
	"testing"

	"github.com/sirupsen/logrus"
)

func TestLogrusLogger(t *testing.T) {
	logger := logrus.New()
	logger.SetLevel(logrus.DebugLevel)
	field := logger.WithField("step", "hello")
	field.Info("hello world - 1")

	span := field.WithField("span", "step 1.1")
	span.Info("hello world - span")

	field.Info("hello world - 2")

	slf4go.GetLoggerFactory().
		GetLogger("test").
		Info("hello world - 3")
}
