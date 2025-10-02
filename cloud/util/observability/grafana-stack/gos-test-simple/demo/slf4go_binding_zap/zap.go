package slf4go_binding_zap

import (
	"fmt"
	"gos-test-simple/demo/slf4go"
	"sync"

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

func (z *ZapLoggingContext) GetRootLogger() slf4go.Logger {
	return z.GetLogger(slf4go.RootLoggerName)
}

func (z *ZapLoggingContext) GetLogger(name string) slf4go.Logger {
	return z.loggers.Get(name, func() slf4go.Logger {
		logger := zap.L()

		return &ZapLogger{
			name:   name,
			level:  slf4go.INFO,
			logger: logger.Sugar().Named(name),
		}
	})
}

func (z *ZapLoggingContext) GetLevel(name string) slf4go.EffectiveLevel {
	return z.loggers.GetLevel(name)
}

func (z *ZapLoggingContext) SetLevel(name string, level slf4go.Level) {
	z.loggers.SetLevel(name, level)
}

type ZapLogger struct {
	name   string
	level  slf4go.Level
	logger *zap.SugaredLogger

	loggerLock sync.RWMutex
}

var _ slf4go.Logger = (*ZapLogger)(nil)

func NewZapLogger(logger *zap.SugaredLogger) ZapLogger {
	return ZapLogger{
		logger: logger,
	}
}

func (l *ZapLogger) getLogger() *zap.SugaredLogger {
	l.loggerLock.RLock()
	prev := l.logger
	l.loggerLock.RUnlock()
	if prev != nil {
		return prev
	}

	l.loggerLock.Lock()
	defer l.loggerLock.Unlock()
	if l.logger != nil {
		l.logger = zap.L().Sugar().Named(l.name)
	}
	return l.logger
}

func (l *ZapLogger) Name() string {
	return l.name
}

func (l *ZapLogger) Level() slf4go.Level {
	return l.level
}

func (l *ZapLogger) SetLevel(level slf4go.Level) slf4go.Logger {
	l.level = level
	//return l
	panic("ZAP SUCKS")
}

func (l *ZapLogger) AtTrace() slf4go.LoggerBuilder {
	return slf4go.LoggerBuilderForLoggerAndLevelUnsafe(l, slf4go.TRACE)
}

func (l *ZapLogger) AtDebug() slf4go.LoggerBuilder {
	return slf4go.LoggerBuilderForLoggerAndLevelUnsafe(l, slf4go.DEBUG)
}

func (l *ZapLogger) AtInfo() slf4go.LoggerBuilder {
	return slf4go.LoggerBuilderForLoggerAndLevelUnsafe(l, slf4go.INFO)
}

func (l *ZapLogger) AtWarn() slf4go.LoggerBuilder {
	return slf4go.LoggerBuilderForLoggerAndLevelUnsafe(l, slf4go.WARN)
}

func (l *ZapLogger) AtError() slf4go.LoggerBuilder {
	return slf4go.LoggerBuilderForLoggerAndLevelUnsafe(l, slf4go.ERROR)
}

func (l *ZapLogger) AtLevel(level slf4go.Level) slf4go.LoggerBuilder {
	return slf4go.LoggerBuilderForLoggerAndLevelUnsafe(l, level)
}

func (l *ZapLogger) Trace(message string) {
	l.logger.Debug(message)
}

func (l *ZapLogger) Debug(message string) {
	l.logger.Debug(message)
}

func (l *ZapLogger) Info(message string) {
	l.logger.Info(message)
}

func (l *ZapLogger) Warn(message string) {
	l.logger.Warn(message)
}

func (l *ZapLogger) Error(message string) {
	l.logger.Error(message)
}

func (l *ZapLogger) IsTraceEnabled() bool {
	return l.logger.Level() >= zapcore.DebugLevel
}

func (l *ZapLogger) IsDebugEnabled() bool {
	return l.logger.Level() >= zapcore.DebugLevel
}

func (l *ZapLogger) IsInfoEnabled() bool {
	return l.logger.Level() >= zapcore.InfoLevel
}

func (l *ZapLogger) IsWarnEnabled() bool {
	return l.logger.Level() >= zapcore.WarnLevel
}

func (l *ZapLogger) IsErrorEnabled() bool {
	return l.logger.Level() >= zapcore.ErrorLevel
}

var zapLevelsMap = map[slf4go.Level]zapcore.Level{
	slf4go.TRACE: zapcore.DebugLevel,
	slf4go.DEBUG: zapcore.DebugLevel,
	slf4go.INFO:  zapcore.InfoLevel,
	slf4go.WARN:  zapcore.WarnLevel,
	slf4go.ERROR: zapcore.ErrorLevel,
}

func (l *ZapLogger) IsEnabled(level slf4go.Level) bool {
	return l.logger.Level() <= zapLevelsMap[level]
}

func (l *ZapLogger) TraceF(message string, args ...any) {
	l.logger.Debugf(message, args...)
}

func (l *ZapLogger) DebugF(message string, args ...any) {
	l.logger.Debugf(message, args)
}

func (l *ZapLogger) InfoF(message string, args ...any) {
	l.logger.Infof(message, args)
}

func (l *ZapLogger) WarnF(message string, args ...any) {
	l.logger.Warnf(message, args)
}

func (l *ZapLogger) ErrorF(message string, args ...any) {
	l.logger.Errorf(message, args)
}

func (l *ZapLogger) TraceError(message string, err error) {
	l.logger.Debugw(message, zap.Error(err))
}

func (l *ZapLogger) DebugError(message string, err error) {
	l.logger.Debugw(message, zap.Error(err))
}

func (l *ZapLogger) InfoError(message string, err error) {
	l.logger.Infow(message, zap.Error(err))
}

func (l *ZapLogger) WarnError(message string, err error) {
	l.logger.Warnw(message, zap.Error(err))
}

func (l *ZapLogger) ErrorError(message string, err error) {
	l.logger.Errorw(message, zap.Error(err))
}

func (l *ZapLogger) TraceErrorF(message string, err error, args ...any) {
	args = append(args, err)
	l.logger.Debugf(message+" (error: %v)", args)
}

func (l *ZapLogger) DebugErrorF(message string, err error, args ...any) {
	args = append(args, err)
	l.logger.Debugf(message+" (error: %v)", args)
}

func (l *ZapLogger) InfoErrorF(message string, err error, args ...any) {
	args = append(args, err)
	l.logger.Debugf(message+" (error: %v)", args)
}

func (l *ZapLogger) WarnErrorF(message string, err error, args ...any) {
	args = append(args, err)
	l.logger.Debugf(message+" (error: %v)", args)
}

func (l *ZapLogger) ErrorErrorF(message string, err error, args ...any) {
	args = append(args, err)
	l.logger.Debugf(message+" (error: %v)", args)
}

func (l *ZapLogger) LevelErrorFKv(level slf4go.Level, message string, err error, kv []slf4go.Kv, args ...any) {
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
