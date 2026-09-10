GO_FILES := go.mod go.sum providerAddress.txt providerVersion.txt $(shell find . -type f -name '*.go')
build/terraform-provider-sshconnection: $(GO_FILES)
	go build -o $@ ./

.PHONY: test
test:
	go test ./...

.PHONY: fmt
fmt:
	go fmt ./...

install_local:
	./install-local.sh

.PHONY: generate_docs
generate_docs:
	go generate -run="go run github.com/hashicorp/terraform-plugin-docs/.*" ./tools/generate.go

build/dev.tfrc: build/terraform-provider-sshconnection providerAddress.txt
	bash -c "echo -e 'provider_installation {\n  dev_overrides {\n    \"'$$(cat providerAddress.txt)'\" = \"'$$(readlink -f ./build)'\"\n  }\n  direct {}\n}' > $@"

.PHONY: dev_tfrc_install
dev_tfrc_install: build/dev.tfrc fs_mirror_uninstall
	./install-dev-tfrc.sh

.PHONY: dev_tfrc_uninstall
dev_tfrc_uninstall:
	./install-dev-tfrc-remove.sh

.PHONY: build_fs_mirror
build_fs_mirror: build/terraform-provider-sshconnection
	./install-fs-mirror.sh

build/fs-mirror.tfrc: providerAddress.txt
	@mkdir -p build
	echo 'provider_installation {'       > build/fs-mirror.tfrc
	echo '  filesystem_mirror {'        >> build/fs-mirror.tfrc
	b=$$(readlink -f build/fs-mirror); \
    echo '    path    = "'$${b}'"'      >> build/fs-mirror.tfrc
	hn=$$(bash -c 'pa=$$(cat providerAddress.txt); echo $${pa%/*}'); \
	echo '    include = ["'$${hn}'/*"]' >> build/fs-mirror.tfrc
	echo '  }'                          >> build/fs-mirror.tfrc
	echo '  direct {'                   >> build/fs-mirror.tfrc
	hn=$$(bash -c 'pa=$$(cat providerAddress.txt); echo $${pa%/*}'); \
	echo '    exclude = ["'$${hn}'/*"]' >> build/fs-mirror.tfrc
	echo '  }'                          >> build/fs-mirror.tfrc
	echo '}'                            >> build/fs-mirror.tfrc
