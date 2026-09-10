.PHONY: test
test:
	go test ./...

.PHONY: generate_docs
generate_docs:
	go generate -run="go run github.com/hashicorp/terraform-plugin-docs/.*" ./tools/generate.go
