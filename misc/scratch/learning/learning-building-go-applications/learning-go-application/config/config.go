package config

import (
	"errors"
	"fmt"
	"learning-go-application/properties"
	"learning-go-application/service"
	"net/http"
	"os"
	"strings"
	"time"

	"github.com/sirupsen/logrus"
	"github.com/spf13/viper"
)

func Configure() *service.App {
	cfg, err := LoadConfig()
	if err != nil {
		panic(err)
	}

	log := logrus.New()
	log.SetOutput(os.Stderr)

	switch cfg.Logging.Format {
	case "text":
		log.SetFormatter(&logrus.TextFormatter{
			FullTimestamp: true,
		})
	case "json":
		log.SetFormatter(&logrus.JSONFormatter{})
	default:
		panic(fmt.Errorf("unknown thing"))
	}

	level, err := logrus.ParseLevel(cfg.Logging.Level)
	if err != nil {
		level = logrus.InfoLevel
	}
	log.SetLevel(level)

	app := &service.App{
		Cfg: cfg,
		Log: log,
		HttpClient: &http.Client{
			Timeout: 5 * time.Second,
		},
	}
	return app
}

func LoadConfig() (properties.Config, error) {
	v := viper.New()

	v.SetDefault("server.address", ":8080")
	v.SetDefault("healthcheck.path", "/healthz")
	v.SetDefault("testRequest.path", "/healthz/proxy")
	v.SetDefault("testRequest.url", "https://jsonplaceholder.typicode.com/todos/1")
	v.SetDefault("logging.level", "info")
	v.SetDefault("logging.format", "text")

	v.SetConfigName("config")
	v.SetConfigType("yaml")
	v.AddConfigPath(".")
	v.AddConfigPath("/etc/healthcheck-app")

	v.SetEnvPrefix("APP")
	v.AutomaticEnv()
	v.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))

	// Env vars:
	// APP_SERVER_ADDRESS=:8080
	// APP_HEALTHCHECK_PATH=/healthz
	// APP_PROXYHEALTHCHECK_PATH=/healthz/proxy
	// APP_PROXYHEALTHCHECK_URL=https://jsonplaceholder.typicode.com/todos/1
	// APP_LOGGING_LEVEL=debug

	if err := v.ReadInConfig(); err != nil {
		var notFound viper.ConfigFileNotFoundError
		if !errors.As(err, &notFound) {
			return properties.Config{}, err
		}
	}

	var cfg properties.Config
	if err := v.Unmarshal(&cfg); err != nil {
		return properties.Config{}, err
	}

	return cfg, nil
}
