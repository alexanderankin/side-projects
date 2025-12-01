package slf4go

import "sync"

var loggerFactory LoggerFactory
var loggerFactorySet bool
var loggerFactoryOnce sync.Once

func GetLoggerFactory() LoggerFactory {
	return loggerFactory
}

func SetLoggerFactory(factory LoggerFactory) {
	loggerFactoryOnce.Do(func() {
		if loggerFactorySet {
			panic("logger factory already set")
		}
		loggerFactory = factory
	})
}

// todo how do we want this to be implemented

// LoggerFactory is meant to be implemented by the frameworks.
// some of the tools that are required like effective level and
// the data structure for a tree of loggers are provided
type LoggerFactory interface {
	FieldNames() *FieldNames
	GetRootLogger() *Logger
	GetLogger(name string) *Logger
	GetLevel(name string) EffectiveLevel
	SetLevel(name string, level Level)
}
