package slf4go

import "github.com/sirupsen/logrus"

type LogrusLogger struct {
	logger logrus.Logger
}

func (l *LogrusLogger) AtTrace() LoggerBuilder {
	return LoggerBuilderForLoggerAndLevelUnsafe(l, TRACE)
}

func (l *LogrusLogger) AtDebug() LoggerBuilder {
	return LoggerBuilderForLoggerAndLevelUnsafe(l, DEBUG)
}

func (l *LogrusLogger) AtInfo() LoggerBuilder {
	return LoggerBuilderForLoggerAndLevelUnsafe(l, INFO)
}

func (l *LogrusLogger) AtWarn() LoggerBuilder {
	return LoggerBuilderForLoggerAndLevelUnsafe(l, WARN)
}

func (l *LogrusLogger) AtError() LoggerBuilder {
	return LoggerBuilderForLoggerAndLevelUnsafe(l, ERROR)
}

func (l *LogrusLogger) AtLevel(level Level) LoggerBuilder {
	return LoggerBuilderForLoggerAndLevelUnsafe(l, level)
}

func (l *LogrusLogger) Trace(message string) {
	l.logger.Trace(message)
}

func (l *LogrusLogger) Debug(message string) {
	l.logger.Debug(message)
}

func (l *LogrusLogger) Info(message string) {
	l.logger.Info(message)
}

func (l *LogrusLogger) Warn(message string) {
	l.logger.Warn(message)
}

func (l *LogrusLogger) Error(message string) {
	l.logger.Error(message)
}

func (l *LogrusLogger) IsTraceEnabled() bool {
	return l.logger.Level >= logrus.TraceLevel
}

func (l *LogrusLogger) IsDebugEnabled() bool {
	return l.logger.Level >= logrus.DebugLevel
}

func (l *LogrusLogger) IsInfoEnabled() bool {
	return l.logger.Level >= logrus.InfoLevel
}

func (l *LogrusLogger) IsWarnEnabled() bool {
	return l.logger.Level >= logrus.WarnLevel
}

func (l *LogrusLogger) IsErrorEnabled() bool {
	return l.logger.Level >= logrus.ErrorLevel
}

var logrusLevelsMap = map[Level]logrus.Level{
	TRACE: logrus.TraceLevel,
	DEBUG: logrus.DebugLevel,
	INFO:  logrus.InfoLevel,
	WARN:  logrus.WarnLevel,
	ERROR: logrus.ErrorLevel,
}

func (l *LogrusLogger) IsEnabled(level Level) bool {
	return l.logger.IsLevelEnabled(logrusLevelsMap[level])
}

func (l *LogrusLogger) TraceF(message string, args ...any) {
	l.logger.Tracef(message, args...)
}

func (l *LogrusLogger) DebugF(message string, args ...any) {
	l.logger.Debugf(message, args)
}

func (l *LogrusLogger) InfoF(message string, args ...any) {
	l.logger.Infof(message, args)
}

func (l *LogrusLogger) WarnF(message string, args ...any) {
	l.logger.Warnf(message, args)
}

func (l *LogrusLogger) ErrorF(message string, args ...any) {
	l.logger.Errorf(message, args)
}

func (l *LogrusLogger) TraceError(message string, err error) {
	l.logger.Logf(logrus.TraceLevel, message, err)
}

func (l *LogrusLogger) DebugError(message string, err error) {
	l.logger.Debugf(message, err)
}

func (l *LogrusLogger) InfoError(message string, err error) {
	l.logger.Infof(message, err)
}

func (l *LogrusLogger) WarnError(message string, err error) {
	l.logger.Warnf(message, err)
}

func (l *LogrusLogger) ErrorError(message string, err error) {
	l.logger.Errorf(message, err)
}

func (l *LogrusLogger) TraceErrorF(message string, err error, args ...any) {
	args = append(args, err)
	l.logger.Tracef(message, args)
}

func (l *LogrusLogger) DebugErrorF(message string, err error, args ...any) {
	args = append(args, err)
	l.logger.Debugf(message, args)
}

func (l *LogrusLogger) InfoErrorF(message string, err error, args ...any) {
	args = append(args, err)
	l.logger.Infof(message, args)
}

func (l *LogrusLogger) WarnErrorF(message string, err error, args ...any) {
	args = append(args, err)
	l.logger.Warnf(message, args)
}

func (l *LogrusLogger) ErrorErrorF(message string, err error, args ...any) {
	args = append(args, err)
	l.logger.Errorf(message, args)
}

func (l *LogrusLogger) LevelErrorFKv(level Level, message string, err error, kv []Kv, args ...any) {
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
