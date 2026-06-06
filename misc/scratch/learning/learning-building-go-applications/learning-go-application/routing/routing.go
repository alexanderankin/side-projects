package routing

import (
	"learning-go-application/service"
	"net/http"

	"github.com/gorilla/mux"
)

func Route(app *service.App) *mux.Router {
	r := mux.NewRouter()
	r.HandleFunc(app.Cfg.Healthcheck.Path, app.Healthcheck).Methods(http.MethodGet)
	r.HandleFunc(app.Cfg.TestRequestUrl.Path, app.ProxyHealthcheck).Methods(http.MethodGet)

	if app.Cfg.Logging.Level == "debug" {
		r.NotFoundHandler = http.HandlerFunc(app.NotFound)
	}

	return r
}
