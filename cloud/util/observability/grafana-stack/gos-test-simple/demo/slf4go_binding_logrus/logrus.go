package slf4go_binding_logrus

import (
	"gos-test-simple/demo/slf4go"

	"github.com/sirupsen/logrus"
)

func init() {
	slf4go.SetLoggerFactory(&LogrusLoggerFactory{
		fieldNames: slf4go.NewFieldNames(),
		loggers:    slf4go.NewLoggers(),
	})
}

type LogrusLoggerFactory struct {
	fieldNames *slf4go.FieldNames
	loggers    *slf4go.Loggers
}

func (l *LogrusLoggerFactory) FieldNames() *slf4go.FieldNames {
	return l.fieldNames
}

func (l *LogrusLoggerFactory) GetRootLogger() slf4go.Logger {
	return l.GetLogger(slf4go.RootLoggerName)
}

func (l *LogrusLoggerFactory) GetLogger(name string) slf4go.Logger {
	return l.loggers.Get(name, func() slf4go.Logger {
		logger := logrus.New()
		logger.SetLevel(logrus.InfoLevel)

		return &LogrusLogger{
			name:   name,
			level:  slf4go.INFO,
			logger: logger,
		}
	})
}

func (l *LogrusLoggerFactory) GetLevel(name string) slf4go.EffectiveLevel {
	//TODO implement me
	panic("implement me")
}

func (l *LogrusLoggerFactory) SetLevel(name string, level slf4go.Level) {
	//TODO implement me
	panic("implement me")
}

var _ slf4go.LoggerFactory = (*LogrusLoggerFactory)(nil)

type LogrusLogger struct {
	name   string
	level  slf4go.Level
	logger logrus.Ext1FieldLogger
}

var _ slf4go.Logger = (*LogrusLogger)(nil)

//func NewLogrusLogger(logger *logrus.Logger, name string) LogrusLogger {
//	return LogrusLogger{
//		logger: logger.WithField(),
//	}
//}

func (l *LogrusLogger) Name() string {
	return l.name
}

func (l *LogrusLogger) Level() slf4go.Level {
	return l.level
}

func (l *LogrusLogger) SetLevel(level slf4go.Level) slf4go.Logger {
	l.level = level
	l.getLogrusLogger().SetLevel(logrusLevelsMap[level])
	return l
}

func (l *LogrusLogger) AtTrace() slf4go.LoggerBuilder {
	return slf4go.LoggerBuilderForLoggerAndLevelUnsafe(l, slf4go.TRACE)
}

func (l *LogrusLogger) AtDebug() slf4go.LoggerBuilder {
	return slf4go.LoggerBuilderForLoggerAndLevelUnsafe(l, slf4go.DEBUG)
}

func (l *LogrusLogger) AtInfo() slf4go.LoggerBuilder {
	return slf4go.LoggerBuilderForLoggerAndLevelUnsafe(l, slf4go.INFO)
}

func (l *LogrusLogger) AtWarn() slf4go.LoggerBuilder {
	return slf4go.LoggerBuilderForLoggerAndLevelUnsafe(l, slf4go.WARN)
}

func (l *LogrusLogger) AtError() slf4go.LoggerBuilder {
	return slf4go.LoggerBuilderForLoggerAndLevelUnsafe(l, slf4go.ERROR)
}

func (l *LogrusLogger) AtLevel(level slf4go.Level) slf4go.LoggerBuilder {
	return slf4go.LoggerBuilderForLoggerAndLevelUnsafe(l, level)
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
	return l.IsEnabled(slf4go.TRACE)
}

func (l *LogrusLogger) IsDebugEnabled() bool {
	return l.IsEnabled(slf4go.DEBUG)
}

func (l *LogrusLogger) IsInfoEnabled() bool {
	return l.IsEnabled(slf4go.INFO)
}

func (l *LogrusLogger) IsWarnEnabled() bool {
	return l.IsEnabled(slf4go.WARN)
}

func (l *LogrusLogger) IsErrorEnabled() bool {
	return l.IsEnabled(slf4go.ERROR)
}

var logrusLevelsMap = map[slf4go.Level]logrus.Level{
	slf4go.TRACE: logrus.TraceLevel,
	slf4go.DEBUG: logrus.DebugLevel,
	slf4go.INFO:  logrus.InfoLevel,
	slf4go.WARN:  logrus.WarnLevel,
	slf4go.ERROR: logrus.ErrorLevel,
}

func (l *LogrusLogger) IsEnabled(level slf4go.Level) bool {
	return l.getLogrusLogger().IsLevelEnabled(logrusLevelsMap[level])
}

func (l *LogrusLogger) TraceF(message string, args ...any) {
	l.logger.Tracef(message, args...)
}

func (l *LogrusLogger) DebugF(message string, args ...any) {
	l.logger.Debugf(message, args...)
}

func (l *LogrusLogger) InfoF(message string, args ...any) {
	l.logger.Infof(message, args...)
}

func (l *LogrusLogger) WarnF(message string, args ...any) {
	l.logger.Warnf(message, args...)
}

func (l *LogrusLogger) ErrorF(message string, args ...any) {
	l.logger.Errorf(message, args...)
}

func (l *LogrusLogger) TraceError(message string, err error) {
	l.logger.Tracef(message, err)
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
	l.logger.Tracef(message+" (error: %v)", args)
}

func (l *LogrusLogger) DebugErrorF(message string, err error, args ...any) {
	args = append(args, err)
	l.logger.Debugf(message+" (error: %v)", args)
}

func (l *LogrusLogger) InfoErrorF(message string, err error, args ...any) {
	args = append(args, err)
	l.logger.Infof(message+" (error: %v)", args)
}

func (l *LogrusLogger) WarnErrorF(message string, err error, args ...any) {
	args = append(args, err)
	l.logger.Warnf(message+" (error: %v)", args)
}

func (l *LogrusLogger) ErrorErrorF(message string, err error, args ...any) {
	args = append(args, err)
	l.logger.Errorf(message+" (error: %v)", args)
}

func (l *LogrusLogger) LevelErrorFKv(level slf4go.Level, message string, err error, kv []slf4go.Kv, args ...any) {
	var logger logrus.FieldLogger = l.logger
	if kv != nil {
		for _, eachKv := range kv {
			logger = logger.WithField(eachKv.Key, eachKv.Value)
		}
	}
	if err != nil {
		logger = logger.WithError(err)
	}
	switch level {
	case slf4go.TRACE:
		logger.Debugf(message, args)
	case slf4go.DEBUG:
		logger.Debugf(message, args)
	case slf4go.INFO:
		logger.Infof(message, args)
	case slf4go.WARN:
		logger.Warnf(message, args)
	case slf4go.ERROR:
		logger.Errorf(message, args)
	}
}

func (l *LogrusLogger) getLogrusLogger() *logrus.Logger {
	var targetLogger *logrus.Logger

	entry, ok := l.logger.(*logrus.Entry)
	if ok {
		targetLogger = entry.Logger
	} else if logger, ok := l.logger.(*logrus.Logger); ok {
		targetLogger = logger
	}
	return targetLogger
}
