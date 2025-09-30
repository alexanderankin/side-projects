package demo

import (
	"context"
	"encoding/base64"
	"errors"
	"fmt"
	"os"
	"time"

	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/exporters/otlp/otlplog/otlploghttp"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetrichttp"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracehttp"
	"go.opentelemetry.io/otel/exporters/stdout/stdoutlog"
	"go.opentelemetry.io/otel/exporters/stdout/stdoutmetric"
	"go.opentelemetry.io/otel/exporters/stdout/stdouttrace"
	"go.opentelemetry.io/otel/log"
	"go.opentelemetry.io/otel/log/global"
	"go.opentelemetry.io/otel/metric"
	sdklog "go.opentelemetry.io/otel/sdk/log"
	sdkmetric "go.opentelemetry.io/otel/sdk/metric"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"
)

type Mode string

const (
	OTel   Mode = "OTel"
	Stdout Mode = "stdout"
)

type ObservabilityConfig struct {
	Url     string
	Headers map[string]string
}

//"http://127.0.0.1:4318/v1/traces"

type ObservabilityConfigReader interface {
	ReadLogConfig() (*ObservabilityConfig, error)
	ReadMetricsConfig() (*ObservabilityConfig, error)
	ReadTracesConfig() (*ObservabilityConfig, error)
}

type LocalObservabilityConfigReader struct {
}

func NewLocalObservabilityConfigReader() *LocalObservabilityConfigReader {
	return &LocalObservabilityConfigReader{}
}

func (l *LocalObservabilityConfigReader) ReadLogConfig() (*ObservabilityConfig, error) {
	input := configInput{user: "open", pass: "telemetry", url: "http://127.0.0.1:4318/v1/logs"}
	return &ObservabilityConfig{Url: input.url, Headers: input.toHeaders()}, nil
}

func (l *LocalObservabilityConfigReader) ReadMetricsConfig() (*ObservabilityConfig, error) {
	input := configInput{user: "open", pass: "telemetry", url: "http://127.0.0.1:4318/v1/metrics"}
	return &ObservabilityConfig{Url: input.url, Headers: input.toHeaders()}, nil
}

func (l *LocalObservabilityConfigReader) ReadTracesConfig() (*ObservabilityConfig, error) {
	input := configInput{user: "open", pass: "telemetry", url: "http://127.0.0.1:4318/v1/traces"}
	return &ObservabilityConfig{Url: input.url, Headers: input.toHeaders()}, nil
}

type EnvVarObservabilityConfigReader struct {
}

func NewObservabilityConfigReader() *EnvVarObservabilityConfigReader {
	return &EnvVarObservabilityConfigReader{}
}

type configInput struct {
	url  string
	user string
	pass string
}

func (c *configInput) toHeaders() map[string]string {
	return map[string]string{"Authorization": "Basic " + base64.StdEncoding.EncodeToString([]byte(c.user+":"+c.pass))}
}

func readConfigInput(prefix string, writeUrl bool) (*configInput, error) {
	var urlSuffix string
	if writeUrl {
		urlSuffix = "_WRITE_URL"
	} else {
		urlSuffix = "_URL"
	}

	var key string
	key = prefix + urlSuffix
	url, ok := os.LookupEnv(key)
	if !ok {
		return nil, fmt.Errorf("missing environment variable: %s", key)
	}

	key = prefix + "_USERNAME"
	user, ok := os.LookupEnv(key)
	if !ok {
		return nil, fmt.Errorf("missing environment variable: %s", key)
	}

	key = prefix + "_PASSWORD"
	pass, ok := os.LookupEnv(key)
	if !ok {
		return nil, fmt.Errorf("missing environment variable: %s", key)
	}

	input := configInput{
		url:  url,
		user: user,
		pass: pass,
	}
	return &input, nil
}

func (*EnvVarObservabilityConfigReader) ReadLogConfig() (*ObservabilityConfig, error) {
	input, err := readConfigInput("GRAFANA_LOKI", true)
	if err != nil {
		return nil, err
	}
	return &ObservabilityConfig{Url: input.url, Headers: input.toHeaders()}, nil
}

func (*EnvVarObservabilityConfigReader) ReadMetricsConfig() (*ObservabilityConfig, error) {
	input, err := readConfigInput("GRAFANA_PROMETHEUS", true)
	if err != nil {
		return nil, err
	}
	return &ObservabilityConfig{Url: input.url, Headers: input.toHeaders()}, nil
}

func (*EnvVarObservabilityConfigReader) ReadTracesConfig() (*ObservabilityConfig, error) {
	input, err := readConfigInput("GRAFANA_TEMPO", false)
	if err != nil {
		return nil, err
	}
	return &ObservabilityConfig{Url: input.url, Headers: input.toHeaders()}, nil
}

func Demo(mode Mode, useLocalAlloy bool) error {
	fmt.Printf("Demo running in %s mode (using local otel config values: %t)\n", mode, useLocalAlloy)

	ctx := context.Background()
	var err error

	var ocr ObservabilityConfigReader
	var oc *ObservabilityConfig
	if mode == OTel {
		if useLocalAlloy {
			ocr = NewLocalObservabilityConfigReader()
		} else {
			ocr = NewObservabilityConfigReader()
		}
	}

	// --- Traces ---
	var traceExp sdktrace.SpanExporter
	switch mode {
	case OTel:
		oc, err = ocr.ReadTracesConfig()
		if err != nil {
			return fmt.Errorf("failed reading traces config: %w", err)
		}

		traceExp, err = otlptrace.New(ctx, otlptracehttp.NewClient(
			otlptracehttp.WithEndpointURL(oc.Url),
			otlptracehttp.WithHeaders(oc.Headers),
		))
	case Stdout:
		traceExp, err = stdouttrace.New(stdouttrace.WithPrettyPrint())
	}
	if err != nil {
		return err
	}

	traceProvider := sdktrace.NewTracerProvider(sdktrace.WithBatcher(traceExp))
	otel.SetTracerProvider(traceProvider)

	// --- Logs ---

	var logExp sdklog.Exporter
	switch mode {
	case OTel:
		oc, err = ocr.ReadLogConfig()
		if err != nil {
			return fmt.Errorf("failed reading logs config: %w", err)
		}

		logExp, err = otlploghttp.New(ctx,
			otlploghttp.WithEndpointURL(oc.Url),
			otlploghttp.WithHeaders(oc.Headers),
		)
		if err != nil {
			return fmt.Errorf("failed creating logs exporter: %w", err)
		}
	case Stdout:
		logExp, err = stdoutlog.New(stdoutlog.WithPrettyPrint())
	}
	if err != nil {
		return err
	}

	logProvider := sdklog.NewLoggerProvider(sdklog.WithProcessor(sdklog.NewBatchProcessor(logExp)))
	global.SetLoggerProvider(logProvider)

	// --- Metrics ---
	var metricExp sdkmetric.Exporter
	switch mode {
	case OTel:
		oc, err = ocr.ReadMetricsConfig()
		if err != nil {
			return fmt.Errorf("failed reading metrics config: %w", err)
		}

		metricExp, err = otlpmetrichttp.New(ctx,
			otlpmetrichttp.WithEndpointURL(oc.Url),
			otlpmetrichttp.WithHeaders(oc.Headers),
		)
	case Stdout:
		metricExp, err = stdoutmetric.New(stdoutmetric.WithPrettyPrint())
	}
	if err != nil {
		return err
	}

	metricReader := sdkmetric.NewPeriodicReader(metricExp)
	metricProvider := sdkmetric.NewMeterProvider(sdkmetric.WithReader(metricReader))
	otel.SetMeterProvider(metricProvider)

	// --- Use them ---
	tracer := otel.Tracer("example-tracer")
	meter := otel.Meter("example-meter")
	logger := global.GetLoggerProvider().Logger("example-logger")

	// Create a trace span
	_, span := tracer.Start(ctx, "demo-span")
	span.SetAttributes(attribute.String("demo.key", "demo-value"))
	defer span.End()

	// Emit a metric
	counter, _ := meter.Int64Counter("demo.counter")
	counter.Add(ctx, 1, metric.WithAttributes(attribute.String("demo.key", "demo-value")))

	record := log.Record{}
	record.SetBody(log.StringValue("Hello from OTel Log!"))
	record.SetTimestamp(time.Now())
	record.AddAttributes(log.KeyValue{Key: "LEVEL", Value: log.StringValue("info")})
	logger.Emit(ctx, record)

	// --- Shutdown and flush ---
	_, _ = fmt.Fprintln(os.Stderr, "Shutting down providers...")
	err1 := logProvider.Shutdown(ctx)
	err2 := metricProvider.Shutdown(ctx)
	err3 := traceProvider.Shutdown(ctx)

	_, _ = fmt.Fprintln(os.Stderr, "Done.")
	return errors.Join(err1, err2, err3)
}
