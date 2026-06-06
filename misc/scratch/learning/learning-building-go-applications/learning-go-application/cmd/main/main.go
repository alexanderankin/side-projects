package main

import (
	"learning-go-application/config"
	"learning-go-application/routing"
	"net/http"
)

func main() {
	app := config.Configure()
	r := routing.Route(app)
	app.Log.WithFields(app.Cfg.MustAsMap()).Info("starting server")

	if err := http.ListenAndServe(app.Cfg.Server.Address, r); err != nil {
		app.Log.WithError(err).Fatal("server stopped")
	}
}
