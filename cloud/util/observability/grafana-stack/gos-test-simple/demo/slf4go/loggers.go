package slf4go

import (
	"strings"
	"sync"
)

const defaultLevel = INFO

var defaultEffectiveLevel EffectiveLevel
var defaultEffectiveLevelOnce = sync.Once{}

type Loggers struct {
	RootLoggerName      string
	LoggerPathSeparator string
	loggers             map[string]Logger
	lock                sync.RWMutex
}

func NewLoggers() *Loggers {
	return &Loggers{
		RootLoggerName:      RootLoggerName,
		LoggerPathSeparator: ".",
		loggers:             make(map[string]Logger),
		lock:                sync.RWMutex{},
	}
}

func (l *Loggers) Get(name string, f func() Logger) Logger {
	l.lock.RLock()
	logger, ok := l.loggers[name]
	l.lock.RUnlock()
	if ok {
		return logger
	}

	l.lock.Lock()
	defer l.lock.Unlock()

	logger, ok = l.loggers[name]
	if ok {
		return logger
	} else {
		logger = f()
		l.loggers[name] = logger
		return logger
	}
}

func (l *Loggers) SetLevel(name string, level Level) {
	l.lock.Lock()
	defer l.lock.Unlock()
	prev, ok := l.loggers[name]
	if ok {
		prev.SetLevel(level)
	} else {

	}
}

func (l *Loggers) GetLevel(name string) EffectiveLevel {
	l.lock.RLock()
	defer l.lock.RUnlock()

	_, ok := l.loggers[name]
	if !ok {
		parts := strings.Split(name, l.LoggerPathSeparator)
		for i := len(parts) - 1; i >= 0; i-- {
			parent := strings.Join(parts[:i], l.LoggerPathSeparator)

			_, parentOk := l.loggers[parent]
			if parentOk {
				panic("not impl")
				//return EffectiveLevel{
				//	Logger:          parentLevel.Logger,
				//	ConfiguredLevel: UNSET,
				//	EffectiveLevel:  parentLevel.ConfiguredLevel,
				//}
			}
		}

		rootLevel, rootOk := l.loggers[l.RootLoggerName]
		if rootOk {
			panic(rootLevel)
			return EffectiveLevel{
				//Logger:          rootLevel.Logger,
				//ConfiguredLevel: UNSET,
				//EffectiveLevel:  rootLevel.ConfiguredLevel,
			}
		}

		return getDefaultEffectiveLevel()
	}
	//return level
	panic("")
}

func getDefaultEffectiveLevel() EffectiveLevel {
	defaultEffectiveLevelOnce.Do(func() {
		defaultEffectiveLevel = EffectiveLevel{ConfiguredLevel: UNSET, EffectiveLevel: defaultLevel}
	})
	return defaultEffectiveLevel
}
