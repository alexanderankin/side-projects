package slf4go_binding_zap

import (
	"fmt"
	"gos-test-simple/demo/slf4go"

	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
)

func init() {
	slf4go.SetLoggerFactory(&ZapLoggingContext{
		fieldNames: slf4go.NewFieldNames(),
		loggers:    slf4go.NewLoggers(),
	})
}

type ZapLoggingContext struct {
	fieldNames *slf4go.FieldNames
	loggers    *slf4go.Loggers
}

var _ slf4go.LoggerFactory = (*ZapLoggingContext)(nil)

func (z *ZapLoggingContext) FieldNames() *slf4go.FieldNames {
	return z.fieldNames
}

func (z *ZapLoggingContext) GetRootLogger() *slf4go.Logger {
	return z.GetLogger(slf4go.RootLoggerName)
}

func (z *ZapLoggingContext) GetLogger(name string) *slf4go.Logger {
	return z.loggers.Get(name, func() *slf4go.Logger {
		logger := zap.L()

		return slf4go.NewLogger(name, slf4go.INFO, &ZapAppender{logger: logger.Sugar().Named(name)})
	})
}

func (z *ZapLoggingContext) GetLevel(name string) slf4go.EffectiveLevel {
	return z.loggers.GetLevel(name)
}

func (z *ZapLoggingContext) SetLevel(name string, level slf4go.Level) {
	z.loggers.SetLevel(name, level)
}

var _ slf4go.Appender = (*ZapAppender)(nil)

type ZapAppender struct {
	logger *zap.SugaredLogger
}

var zapLevelsMap = map[slf4go.Level]zapcore.Level{
	slf4go.TRACE: zapcore.DebugLevel,
	slf4go.DEBUG: zapcore.DebugLevel,
	slf4go.INFO:  zapcore.InfoLevel,
	slf4go.WARN:  zapcore.WarnLevel,
	slf4go.ERROR: zapcore.ErrorLevel,
}

func (l *ZapAppender) IsEnabled(level slf4go.Level) bool {
	return l.logger.Level() <= zapLevelsMap[level]
}

func (l *ZapAppender) Log(level slf4go.Level, message string, err error, kv []slf4go.Kv, args ...any) {
	if !l.IsEnabled(level) {
		return
	}

	switch level {
	case slf4go.TRACE:
		l.logger.Debugw(fmt.Sprint(message, args), append(kvListToZapFieldList(kv), (interface{})(zap.Error(err)))...)
	case slf4go.DEBUG:
		l.logger.Debugw(fmt.Sprint(message, args), append(kvListToZapFieldList(kv), (interface{})(zap.Error(err)))...)
	case slf4go.INFO:
		l.logger.Infow(fmt.Sprint(message, args), append(kvListToZapFieldList(kv), (interface{})(zap.Error(err)))...)
	case slf4go.WARN:
		l.logger.Warnw(fmt.Sprint(message, args), append(kvListToZapFieldList(kv), (interface{})(zap.Error(err)))...)
	case slf4go.ERROR:
		l.logger.Errorw(fmt.Sprint(message, args), append(kvListToZapFieldList(kv), (interface{})(zap.Error(err)))...)
	}
}

func kvListToZapFieldList(kvList []slf4go.Kv) []interface{} {
	fields := make([]interface{}, 0, len(kvList))
	for _, kv := range kvList {
		fields = append(fields, zap.String(kv.Key, kv.Value))
	}
	return fields
}
