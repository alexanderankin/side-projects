package service

import (
	"context"
	"encoding/json"
	"io"
	"learning-go-application/properties"
	"net/http"
	"time"

	"github.com/sirupsen/logrus"
)

type App struct {
	Cfg        properties.Config
	Log        *logrus.Logger
	HttpClient *http.Client
}

func writeJSON(w http.ResponseWriter, statusCode int, value any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(statusCode)

	_ = json.NewEncoder(w).Encode(value)
}

func (a *App) writeJSONError(w http.ResponseWriter, statusCode int, err error, errorMsg string) {
	a.Log.WithError(err).Warn(errorMsg)
	writeJSON(w, statusCode, map[string]string{"status": "error", "error": errorMsg})
}

func (a *App) Healthcheck(w http.ResponseWriter, r *http.Request) {
	a.Log.WithFields(logrus.Fields{
		"path":   r.URL.Path,
		"method": r.Method,
	}).Debug("running healthcheck")

	writeJSON(w, http.StatusOK, map[string]string{
		"status": "ok",
		"time":   time.Now().UTC().Format(time.RFC3339),
	})
}

func (a *App) ProxyHealthcheck(w http.ResponseWriter, r *http.Request) {
	a.Log.WithFields(logrus.Fields{
		"path": r.URL.Path,
		"url":  a.Cfg.TestRequestUrl.URL,
	}).Debug("performing test request")

	ctx, cancel := context.WithTimeout(r.Context(), 5*time.Second)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, http.MethodGet, a.Cfg.TestRequestUrl.URL, nil)
	if err != nil {
		a.writeJSONError(w, http.StatusInternalServerError, err, "error creating test request: "+err.Error())
		return
	}

	resp, err := a.HttpClient.Do(req)
	if err != nil {
		a.writeJSONError(w, http.StatusServiceUnavailable, err, "error performing test request: "+err.Error())
		return
	}
	defer func(Body io.ReadCloser) {
		_ = Body.Close()
	}(resp.Body)

	upstreamBody, err := io.ReadAll(resp.Body)
	if err != nil {
		a.writeJSONError(w, http.StatusBadGateway, err, "failed reading body: "+err.Error())
		return
	}

	status := "ok"
	code := http.StatusOK
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		status = "error"
		code = http.StatusBadGateway
	}

	writeJSON(w, code, map[string]any{
		"status":      status,
		"upstreamUrl": a.Cfg.TestRequestUrl.URL,
		"statusCode":  resp.StatusCode,
		"body":        upstreamBody,
	})
}

func (a *App) NotFound(writer http.ResponseWriter, request *http.Request) {
	a.Log.WithFields(logrus.Fields{
		"method":     request.Method,
		"path":       request.URL.Path,
		"remoteAddr": request.RemoteAddr,
		"userAgent":  request.UserAgent(),
	}).Debug("request not found")

	http.NotFoundHandler().ServeHTTP(writer, request)
}
