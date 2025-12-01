package slf4go

import "context"

type Logger struct {
	appender Appender
	name     string
	level    level
}

func NewLogger(name string, level level, appender Appender) *Logger {
	return &Logger{name: name, level: level, appender: appender}
}

func (l *Logger) Appender() Appender {
	return l.appender
}

func (l *Logger) WithAppender(appender Appender) *Logger {
	return &Logger{appender: appender, name: l.name, level: l.level}
}

func (l *Logger) Name() string {
	return l.name
}

func (l *Logger) WithName(name string) *Logger {
	return &Logger{appender: l.appender, name: name, level: l.level}
}

func (l *Logger) Level() Level {
	return l.level
}

func (l *Logger) SetLevel(level Level) *Logger {
	l.level = level
	return l
}

func (l *Logger) Trace(ctx context.Context, message string) {
	l.appender.Log(ctx, TRACE, message, nil, nil)
}
func (l *Logger) Debug(ctx context.Context, message string) {
	l.appender.Log(ctx, DEBUG, message, nil, nil)
}
func (l *Logger) Info(ctx context.Context, message string) {
	l.appender.Log(ctx, INFO, message, nil, nil)
}
func (l *Logger) Warn(ctx context.Context, message string) {
	l.appender.Log(ctx, WARN, message, nil, nil)
}
func (l *Logger) Error(ctx context.Context, message string) {
	l.appender.Log(ctx, ERROR, message, nil, nil)
}

func (l *Logger) TraceF(ctx context.Context, message string, args ...any) {
	l.appender.Log(ctx, TRACE, message, nil, nil, args...)
}
func (l *Logger) DebugF(ctx context.Context, message string, args ...any) {
	l.appender.Log(ctx, DEBUG, message, nil, nil, args...)
}
func (l *Logger) InfoF(ctx context.Context, message string, args ...any) {
	l.appender.Log(ctx, INFO, message, nil, nil, args...)
}
func (l *Logger) WarnF(ctx context.Context, message string, args ...any) {
	l.appender.Log(ctx, WARN, message, nil, nil, args...)
}
func (l *Logger) ErrorF(ctx context.Context, message string, args ...any) {
	l.appender.Log(ctx, ERROR, message, nil, nil, args...)
}

func (l *Logger) TraceKv(ctx context.Context, message string, kv []Kv) {
	l.appender.Log(ctx, TRACE, message, nil, kv)
}
func (l *Logger) DebugKv(ctx context.Context, message string, kv []Kv) {
	l.appender.Log(ctx, DEBUG, message, nil, kv)
}
func (l *Logger) InfoKv(ctx context.Context, message string, kv []Kv) {
	l.appender.Log(ctx, INFO, message, nil, kv)
}
func (l *Logger) WarnKv(ctx context.Context, message string, kv []Kv) {
	l.appender.Log(ctx, WARN, message, nil, kv)
}
func (l *Logger) ErrorKv(ctx context.Context, message string, kv []Kv) {
	l.appender.Log(ctx, ERROR, message, nil, kv)
}

func (l *Logger) TraceFKv(ctx context.Context, message string, kv []Kv, args ...any) {
	l.appender.Log(ctx, TRACE, message, nil, kv, args...)
}
func (l *Logger) DebugFKv(ctx context.Context, message string, kv []Kv, args ...any) {
	l.appender.Log(ctx, DEBUG, message, nil, kv, args...)
}
func (l *Logger) InfoFKv(ctx context.Context, message string, kv []Kv, args ...any) {
	l.appender.Log(ctx, INFO, message, nil, kv, args...)
}
func (l *Logger) WarnFKv(ctx context.Context, message string, kv []Kv, args ...any) {
	l.appender.Log(ctx, WARN, message, nil, kv, args...)
}
func (l *Logger) ErrorFKv(ctx context.Context, message string, kv []Kv, args ...any) {
	l.appender.Log(ctx, ERROR, message, nil, kv, args...)
}

func (l *Logger) TraceErr(ctx context.Context, message string, err error) {
	l.appender.Log(ctx, TRACE, message, err, nil)
}
func (l *Logger) DebugErr(ctx context.Context, message string, err error) {
	l.appender.Log(ctx, DEBUG, message, err, nil)
}
func (l *Logger) InfoErr(ctx context.Context, message string, err error) {
	l.appender.Log(ctx, INFO, message, err, nil)
}
func (l *Logger) WarnErr(ctx context.Context, message string, err error) {
	l.appender.Log(ctx, WARN, message, err, nil)
}
func (l *Logger) ErrorErr(ctx context.Context, message string, err error) {
	l.appender.Log(ctx, ERROR, message, err, nil)
}

func (l *Logger) TraceFErr(ctx context.Context, message string, err error, args ...any) {
	l.appender.Log(ctx, TRACE, message, err, nil, args...)
}
func (l *Logger) DebugFErr(ctx context.Context, message string, err error, args ...any) {
	l.appender.Log(ctx, DEBUG, message, err, nil, args...)
}
func (l *Logger) InfoFErr(ctx context.Context, message string, err error, args ...any) {
	l.appender.Log(ctx, INFO, message, err, nil, args...)
}
func (l *Logger) WarnFErr(ctx context.Context, message string, err error, args ...any) {
	l.appender.Log(ctx, WARN, message, err, nil, args...)
}
func (l *Logger) ErrorFErr(ctx context.Context, message string, err error, args ...any) {
	l.appender.Log(ctx, ERROR, message, err, nil, args...)
}

func (l *Logger) TraceKvErr(ctx context.Context, message string, kv []Kv, err error) {
	l.appender.Log(ctx, TRACE, message, err, kv)
}
func (l *Logger) DebugKvErr(ctx context.Context, message string, kv []Kv, err error) {
	l.appender.Log(ctx, DEBUG, message, err, kv)
}
func (l *Logger) InfoKvErr(ctx context.Context, message string, kv []Kv, err error) {
	l.appender.Log(ctx, INFO, message, err, kv)
}
func (l *Logger) WarnKvErr(ctx context.Context, message string, kv []Kv, err error) {
	l.appender.Log(ctx, WARN, message, err, kv)
}
func (l *Logger) ErrorKvErr(ctx context.Context, message string, kv []Kv, err error) {
	l.appender.Log(ctx, ERROR, message, err, kv)
}

func (l *Logger) TraceFKvErr(ctx context.Context, message string, kv []Kv, err error, args ...any) {
	l.appender.Log(ctx, TRACE, message, err, kv, args...)
}
func (l *Logger) DebugFKvErr(ctx context.Context, message string, kv []Kv, err error, args ...any) {
	l.appender.Log(ctx, DEBUG, message, err, kv, args...)
}
func (l *Logger) InfoFKvErr(ctx context.Context, message string, kv []Kv, err error, args ...any) {
	l.appender.Log(ctx, INFO, message, err, kv, args...)
}
func (l *Logger) WarnFKvErr(ctx context.Context, message string, kv []Kv, err error, args ...any) {
	l.appender.Log(ctx, WARN, message, err, kv, args...)
}
func (l *Logger) ErrorFKvErr(ctx context.Context, message string, kv []Kv, err error, args ...any) {
	l.appender.Log(ctx, ERROR, message, err, kv, args...)
}

func (l *Logger) AtTrace() LoggerBuilder {
	return &DefaultLoggerBuilder{level: TRACE, logger: l}
}
func (l *Logger) AtDebug() LoggerBuilder {
	return &DefaultLoggerBuilder{level: DEBUG, logger: l}
}
func (l *Logger) AtInfo() LoggerBuilder {
	return &DefaultLoggerBuilder{level: INFO, logger: l}
}
func (l *Logger) AtWarn() LoggerBuilder {
	return &DefaultLoggerBuilder{level: WARN, logger: l}
}
func (l *Logger) AtError() LoggerBuilder {
	return &DefaultLoggerBuilder{level: ERROR, logger: l}
}
