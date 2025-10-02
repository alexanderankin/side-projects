package slf4go

type FieldNames struct {
	Logger string
	Level  string
	//StackTrace string // not implemented initially, its go
	//Tags       string
	//KvPairs    string // if present - log kv pairs as one object, otherwise log all on root object
}

func NewFieldNames() *FieldNames {
	return (&FieldNames{}).Reset()
}

func (f *FieldNames) Reset() *FieldNames {
	f.Logger = "logger_name"
	f.Level = "level"
	//f.StackTrace = "stack_trace"
	return f
}
