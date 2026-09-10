package main

import (
	"context"
	_ "embed"
	"flag"
	"log"
	"strings"

	"github.com/hashicorp/terraform-plugin-framework/providerserver"
	"github.com/toor/terraform-provider-ssh-connection/internal/provider"
)

//go:embed providerVersion.txt
var version string

//go:embed providerAddress.txt
var providerAddress string

func main() {
	var debug bool
	flag.BoolVar(&debug, "debug", false, "run the provider with debugger support")
	flag.Parse()

	err := providerserver.Serve(context.Background(), provider.New(version), providerserver.ServeOpts{
		// you might choose to use terraform.example.com as your placeholder hostname, even if that hostname doesn't actually resolve in DNS
		// https://developer.hashicorp.com/terraform/language/providers/requirements
		Address: strings.TrimSpace(providerAddress),
		Debug:   debug,
	})
	if err != nil {
		log.Fatal(err)
	}
}
