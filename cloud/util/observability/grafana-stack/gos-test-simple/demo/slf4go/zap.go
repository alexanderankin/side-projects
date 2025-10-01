package slf4go

import (
	"fmt"

	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
)

type ZapLogger struct {
	logger *zap.SugaredLogger
}

func NewZapLogger(logger *zap.SugaredLogger) ZapLogger {
	return ZapLogger{
		logger: logger,
	}
}

func (l *ZapLogger) AtTrace() LoggerBuilder {
	return LoggerBuilderForLoggerAndLevelUnsafe(l, TRACE)
}

func (l *ZapLogger) AtDebug() LoggerBuilder {
	return LoggerBuilderForLoggerAndLevelUnsafe(l, DEBUG)
}

func (l *ZapLogger) AtInfo() LoggerBuilder {
	return LoggerBuilderForLoggerAndLevelUnsafe(l, INFO)
}

func (l *ZapLogger) AtWarn() LoggerBuilder {
	return LoggerBuilderForLoggerAndLevelUnsafe(l, WARN)
}

func (l *ZapLogger) AtError() LoggerBuilder {
	return LoggerBuilderForLoggerAndLevelUnsafe(l, ERROR)
}

func (l *ZapLogger) AtLevel(level Level) LoggerBuilder {
	return LoggerBuilderForLoggerAndLevelUnsafe(l, level)
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

var zapLevelsMap = map[Level]zapcore.Level{
	TRACE: zapcore.DebugLevel,
	DEBUG: zapcore.DebugLevel,
	INFO:  zapcore.InfoLevel,
	WARN:  zapcore.WarnLevel,
	ERROR: zapcore.ErrorLevel,
}

func (l *ZapLogger) IsEnabled(level Level) bool {
	return l.logger.Level() >= zapLevelsMap[level]
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

func (l *ZapLogger) LevelErrorFKv(level Level, message string, err error, kv []Kv, args ...any) {
	if !l.IsEnabled(level) {
		return
	}

	switch level {
	case TRACE:
		l.logger.Debugw(fmt.Sprint(message, args), append(kvListToZapFieldList(kv), zap.Error(err)))
	case DEBUG:
		l.logger.Debugw(fmt.Sprint(message, args), append(kvListToZapFieldList(kv), zap.Error(err)))
	case INFO:
		l.logger.Infow(fmt.Sprint(message, args), append(kvListToZapFieldList(kv), zap.Error(err)))
	case WARN:
		l.logger.Warnw(fmt.Sprint(message, args), append(kvListToZapFieldList(kv), zap.Error(err)))
	case ERROR:
		l.logger.Errorw(fmt.Sprint(message, args), append(kvListToZapFieldList(kv), zap.Error(err)))
	}
}

func kvListToZapFieldList(kvList []Kv) []zap.Field {
	fields := make([]zap.Field, len(kvList))
	for _, kv := range kvList {
		fields = append(fields, zap.String(kv.key, kv.value))
	}
	return fields
}
