package slf4go

import (
	"github.com/sirupsen/logrus"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
)

type ZapLogger struct {
	logger zap.SugaredLogger
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
	l.logger.Debugf(message, args...)

	args = append(args, err)
	l.logger.Tracef(message, args)
}

func (l *ZapLogger) DebugErrorF(message string, err error, args ...any) {
	args = append(args, err)
	l.logger.Debugf(message, args)
}

func (l *ZapLogger) InfoErrorF(message string, err error, args ...any) {
	args = append(args, err)
	l.logger.Infof(message, args)
}

func (l *ZapLogger) WarnErrorF(message string, err error, args ...any) {
	args = append(args, err)
	l.logger.Warnf(message, args)
}

func (l *ZapLogger) ErrorErrorF(message string, err error, args ...any) {
	args = append(args, err)
	l.logger.Errorf(message, args)
}

func (l *ZapLogger) LevelErrorFKv(level Level, message string, err error, kv []Kv, args ...any) {
	var logger logrus.FieldLogger = &l.logger
	if kv != nil {
		for _, eachKv := range kv {
			logger = logger.WithField(eachKv.key, eachKv.value)
		}
	}
	if err != nil {
		logger = logger.WithError(err)
	}
	switch level {
	case TRACE:
		logger.Debugf(message, args)
	case DEBUG:
		logger.Debugf(message, args)
	case INFO:
		logger.Infof(message, args)
	case WARN:
		logger.Warnf(message, args)
	case ERROR:
		logger.Errorf(message, args)
	}
}
