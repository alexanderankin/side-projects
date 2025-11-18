package slf4go

import "errors"

type level string
type Level = level

const (
	UNSET level = ""
	TRACE level = "TRACE"
	DEBUG level = "DEBUG"
	INFO  level = "INFO"
	WARN  level = "WARN"
	ERROR level = "ERROR"
)

const RootLoggerName = "ROOT"

type EffectiveLevel struct {
	Logger          Logger
	ConfiguredLevel Level
	EffectiveLevel  Level
}

type Appender interface {
	Log(level Level, message string, err error, kv []Kv, args ...any)
}

type LoggerBuilder interface {
	AddKeyValue(key string, value string) LoggerBuilder

	Message(string) LoggerBuilder

	MessageF(message string, args ...any) LoggerBuilder

	Error(err error) LoggerBuilder

	Log()
}

// LoggerBuilderForLoggerAndLevel is a helper for implementers of Logger
func LoggerBuilderForLoggerAndLevel(logger Logger, level Level) (LoggerBuilder, error) {
	if logger == nil {
		return nil, errors.New("logger is nil")
	}
	if level == "" {
		return nil, errors.New("level is empty")
	}
	return &DefaultLoggerBuilder{logger: logger, level: level}, nil
}

func LoggerBuilderForLoggerAndLevelUnsafe(logger Logger, level Level) LoggerBuilder {
	if logger == nil {
		panic(errors.New("logger is nil"))
	}
	if level == "" {
		panic(errors.New("level is empty"))
	}
	return &DefaultLoggerBuilder{logger: logger, level: level}
}

type Kv struct {
	Key   string
	Value string
}

type DefaultLoggerBuilder struct {
	level   Level
	message string
	kvPairs []Kv
	args    []any
	logger  Logger
	err     error
}

func (l *DefaultLoggerBuilder) AddKeyValue(key string, value string) LoggerBuilder {
	if l.kvPairs == nil {
		l.kvPairs = make([]Kv, 0, 5)
	}
	l.kvPairs = append(l.kvPairs, Kv{key, value})
	return l
}

func (l *DefaultLoggerBuilder) Message(message string) LoggerBuilder {
	l.message = message
	return l
}

func (l *DefaultLoggerBuilder) MessageF(message string, args ...any) LoggerBuilder {
	l.message = message
	l.args = args
	return l
}

func (l *DefaultLoggerBuilder) Error(err error) LoggerBuilder {
	l.err = err
	return l
}

func (l *DefaultLoggerBuilder) Log() {
	l.logger.LevelErrorFKv(l.level, l.message, l.err, l.kvPairs, l.args...)
}
