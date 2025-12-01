package slf4go

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

func (l *Logger) Trace(message string) {
	l.appender.Log(TRACE, message, nil, nil)
}
func (l *Logger) Debug(message string) {
	l.appender.Log(DEBUG, message, nil, nil)
}
func (l *Logger) Info(message string) {
	l.appender.Log(INFO, message, nil, nil)
}
func (l *Logger) Warn(message string) {
	l.appender.Log(WARN, message, nil, nil)
}
func (l *Logger) Error(message string) {
	l.appender.Log(ERROR, message, nil, nil)
}

func (l *Logger) TraceF(message string, args ...any) {
	l.appender.Log(TRACE, message, nil, nil, args...)
}
func (l *Logger) DebugF(message string, args ...any) {
	l.appender.Log(DEBUG, message, nil, nil, args...)
}
func (l *Logger) InfoF(message string, args ...any) {
	l.appender.Log(INFO, message, nil, nil, args...)
}
func (l *Logger) WarnF(message string, args ...any) {
	l.appender.Log(WARN, message, nil, nil, args...)
}
func (l *Logger) ErrorF(message string, args ...any) {
	l.appender.Log(ERROR, message, nil, nil, args...)
}

func (l *Logger) TraceKv(message string, kv []Kv) {
	l.appender.Log(TRACE, message, nil, kv)
}
func (l *Logger) DebugKv(message string, kv []Kv) {
	l.appender.Log(DEBUG, message, nil, kv)
}
func (l *Logger) InfoKv(message string, kv []Kv) {
	l.appender.Log(INFO, message, nil, kv)
}
func (l *Logger) WarnKv(message string, kv []Kv) {
	l.appender.Log(WARN, message, nil, kv)
}
func (l *Logger) ErrorKv(message string, kv []Kv) {
	l.appender.Log(ERROR, message, nil, kv)
}

func (l *Logger) TraceFKv(message string, kv []Kv, args ...any) {
	l.appender.Log(TRACE, message, nil, kv, args...)
}
func (l *Logger) DebugFKv(message string, kv []Kv, args ...any) {
	l.appender.Log(DEBUG, message, nil, kv, args...)
}
func (l *Logger) InfoFKv(message string, kv []Kv, args ...any) {
	l.appender.Log(INFO, message, nil, kv, args...)
}
func (l *Logger) WarnFKv(message string, kv []Kv, args ...any) {
	l.appender.Log(WARN, message, nil, kv, args...)
}
func (l *Logger) ErrorFKv(message string, kv []Kv, args ...any) {
	l.appender.Log(ERROR, message, nil, kv, args...)
}

func (l *Logger) TraceErr(message string, err error) {
	l.appender.Log(TRACE, message, err, nil)
}
func (l *Logger) DebugErr(message string, err error) {
	l.appender.Log(DEBUG, message, err, nil)
}
func (l *Logger) InfoErr(message string, err error) {
	l.appender.Log(INFO, message, err, nil)
}
func (l *Logger) WarnErr(message string, err error) {
	l.appender.Log(WARN, message, err, nil)
}
func (l *Logger) ErrorErr(message string, err error) {
	l.appender.Log(ERROR, message, err, nil)
}

func (l *Logger) TraceFErr(message string, err error, args ...any) {
	l.appender.Log(TRACE, message, err, nil, args...)
}
func (l *Logger) DebugFErr(message string, err error, args ...any) {
	l.appender.Log(DEBUG, message, err, nil, args...)
}
func (l *Logger) InfoFErr(message string, err error, args ...any) {
	l.appender.Log(INFO, message, err, nil, args...)
}
func (l *Logger) WarnFErr(message string, err error, args ...any) {
	l.appender.Log(WARN, message, err, nil, args...)
}
func (l *Logger) ErrorFErr(message string, err error, args ...any) {
	l.appender.Log(ERROR, message, err, nil, args...)
}

func (l *Logger) TraceKvErr(message string, kv []Kv, err error) {
	l.appender.Log(TRACE, message, err, kv)
}
func (l *Logger) DebugKvErr(message string, kv []Kv, err error) {
	l.appender.Log(DEBUG, message, err, kv)
}
func (l *Logger) InfoKvErr(message string, kv []Kv, err error) {
	l.appender.Log(INFO, message, err, kv)
}
func (l *Logger) WarnKvErr(message string, kv []Kv, err error) {
	l.appender.Log(WARN, message, err, kv)
}
func (l *Logger) ErrorKvErr(message string, kv []Kv, err error) {
	l.appender.Log(ERROR, message, err, kv)
}

func (l *Logger) TraceFKvErr(message string, kv []Kv, err error, args ...any) {
	l.appender.Log(TRACE, message, err, kv, args...)
}
func (l *Logger) DebugFKvErr(message string, kv []Kv, err error, args ...any) {
	l.appender.Log(DEBUG, message, err, kv, args...)
}
func (l *Logger) InfoFKvErr(message string, kv []Kv, err error, args ...any) {
	l.appender.Log(INFO, message, err, kv, args...)
}
func (l *Logger) WarnFKvErr(message string, kv []Kv, err error, args ...any) {
	l.appender.Log(WARN, message, err, kv, args...)
}
func (l *Logger) ErrorFKvErr(message string, kv []Kv, err error, args ...any) {
	l.appender.Log(ERROR, message, err, kv, args...)
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
