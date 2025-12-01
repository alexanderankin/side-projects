package slf4go_binding_logrus

import (
	"context"
	"gos-test-simple/demo/slf4go"

	"github.com/sirupsen/logrus"
)

type LogrusAppender struct {
	logrusLogger logrus.Ext1FieldLogger
}

var _ slf4go.Appender = (*LogrusAppender)(nil)

func (l *LogrusAppender) Log(ctx context.Context, level slf4go.Level, message string, err error, kv []slf4go.Kv, args ...any) {
	logger := l.logrusLogger
	if kv != nil {
		for _, eachKv := range kv {
			logger = logger.WithField(eachKv.Key, eachKv.Value)
		}
	}
	if err != nil {
		logger = logger.WithError(err)
	}
	if args != nil && len(args) > 0 {
		switch level {
		case slf4go.TRACE:
			logger.Tracef(message, args)
		case slf4go.DEBUG:
			logger.Debugf(message, args)
		case slf4go.INFO:
			logger.Infof(message, args)
		case slf4go.WARN:
			logger.Warnf(message, args)
		case slf4go.ERROR:
			logger.Errorf(message, args)
		}
	} else {
		switch level {
		case slf4go.TRACE:
			logger.Trace(message)
		case slf4go.DEBUG:
			logger.Debug(message)
		case slf4go.INFO:
			logger.Info(message)
		case slf4go.WARN:
			logger.Warn(message)
		case slf4go.ERROR:
			logger.Error(message)
		}
	}
}
