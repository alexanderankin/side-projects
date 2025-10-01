package demo

import (
	"os"

	"github.com/sirupsen/logrus"
	"go.uber.org/zap/zapcore"

	"go.uber.org/zap"
)

type SupportedLoggingFramework string

const (
	LogRus SupportedLoggingFramework = "LogRus"
	Zap    SupportedLoggingFramework = "Zap"
)

func LoggingDemo() {
	logrus.Println("Logging demo")
	zap.New(
		zapcore.NewCore(
			zapcore.NewJSONEncoder(zapcore.EncoderConfig{}),
			zapcore.AddSync(os.Stdout),
			zapcore.DebugLevel,
		),
	)
}
