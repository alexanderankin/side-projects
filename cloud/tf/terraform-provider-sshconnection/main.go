package main

import (
	"context"
	"flag"
	"log"

	"github.com/hashicorp/terraform-plugin-framework/providerserver"
	"github.com/toor/terraform-provider-ssh-connection/internal/provider"
)

var version = "dev"

func main() {
	var debug bool
	flag.BoolVar(&debug, "debug", false, "run the provider with debugger support")
	flag.Parse()

	err := providerserver.Serve(context.Background(), provider.New(version), providerserver.ServeOpts{
		// you might choose to use terraform.example.com as your placeholder hostname, even if that hostname doesn't actually resolve in DNS
		// https://developer.hashicorp.com/terraform/language/providers/requirements
		Address: "terraform.example.com/side-projects/sshconnection",
		Debug:   debug,
	})
	if err != nil {
		log.Fatal(err)
	}
}
