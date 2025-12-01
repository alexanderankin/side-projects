package slf4go_binding_zap_test

import (
	"context"
	"gos-test-simple/demo/slf4go"
	_ "gos-test-simple/demo/slf4go_binding_zap"
	"testing"

	"go.uber.org/zap"
)

func init() {
	d, err := zap.NewDevelopment()
	if err != nil {
		panic(err)
	}
	zap.ReplaceGlobals(d)
}

func TestName(t *testing.T) {
	logger := slf4go.GetLoggerFactory().GetLogger("TestName")
	logger.
		AtInfo().
		AddKeyValue("key", "value").
		Message("hello").
		Log(context.Background())
}
