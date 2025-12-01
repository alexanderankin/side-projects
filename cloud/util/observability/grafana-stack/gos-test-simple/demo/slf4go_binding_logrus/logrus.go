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
	rootLogger logrus.Ext1FieldLogger
}

func (l *LogrusLoggerFactory) FieldNames() *slf4go.FieldNames {
	return l.fieldNames
}

func (l *LogrusLoggerFactory) GetRootLogger() *slf4go.Logger {
	return l.GetLogger(slf4go.RootLoggerName)
}

func (l *LogrusLoggerFactory) GetLogger(name string) *slf4go.Logger {
	return l.loggers.Get(name, func() *slf4go.Logger {
		logger := logrus.New()
		if l.rootLogger != nil {
			logger.SetLevel((l.rootLogger.(*logrus.Logger)).Level)
		} else {
			logger.SetLevel(logrus.InfoLevel)
		}
		//l.rootLogger.(logrus.Entry).Level
		//l.rootLogger.(logrus.Logger).Level

		return slf4go.NewLogger(name, slf4go.INFO, &LogrusAppender{logrusLogger: logger})
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
