IMAGE ?= ghcr.io/alexanderankin/side-projects-learning-build-go-application
TAG   ?= 0.0.1-SNAPSHOT

GO_FILES := $(shell find . -type f -name '*.go')

main: $(GO_FILES)
	go build ./cmd/main

run: main
	./main

run-with-config-yaml: main
	cp config.example.yaml build/config.yaml && cd build && ../main

run-with-env-vars: main
	bash -c 'set -o allexport; source config.example.env; ./main'


# linux: sudo apt-get install -y gcc-aarch64-linux-gnu gcc-arm-linux-gnueabihf gcc-riscv64-linux-gnu
# mac: brew tap messense/macos-cross-toolchains && brew install x86_64-unknown-linux-gnu aarch64-unknown-linux-gnu
build/static-amd64: $(GO_FILES)
	@mkdir -p build
	CC=x86_64-linux-gnu-gcc CGO_ENABLED=1 GOOS=linux GOARCH=amd64 \
	  go build -v -tags netgo,osusergo -ldflags '-linkmode external -extldflags -static' \
	    -o ./build/static-amd64 ./cmd/main

build/static-arm64: $(GO_FILES)
	@mkdir -p build
	CC=aarch64-linux-gnu-gcc CGO_ENABLED=1 GOOS=linux GOARCH=arm64 \
	  go build -v -tags netgo,osusergo -ldflags '-linkmode external -extldflags -static' \
	    -o ./build/static-arm64 ./cmd/main

JIB_VERSION ?= 0.13.0
build/.jib-version:
	@mkdir -p build
	@echo $(JIB_VERSION) > $@
build/.jib-installed: build/.jib-version
	bash -c -eu "true \
	  && { [[ \"\$$PATH\" =~ .*:\"\$$HOME/.local/bin\":.* ]] || { echo \"error: can't find ~/.local/bin in PATH, exiting\"; exit 1; }; } \
	  && (cd build && wget -N https://github.com/GoogleContainerTools/jib/releases/download/v\$$(cat .jib-version)-cli/jib-jre-\$$(cat .jib-version).zip) \
	  && (cd build && unzip -n jib-jre-\$$(cat .jib-version).zip) \
	  && ln -fs \$$PWD/build/jib-\$$(cat build/.jib-version)/bin/jib \$$HOME/.local/bin/jib \
	  && jib --version \
	  && touch $@ \
	  && :;"

# ensure scopes:
# gh auth refresh -h github.com -s write:packages,read:packages
GHCR_USER ?= $(shell gh api user --jq .login)
GHCR_PAT  ?= $(shell gh auth token)

docker-images: build/static-amd64 build/static-arm64 build/.jib-installed
	bash -c -eux "jib build --build-file jib.yaml --parameter arch=amd64 --target $(IMAGE):$(TAG)-amd64 --to-username $(GHCR_USER) --to-password \"$(GHCR_PAT)\"  \$$(if [ -n \$$SRC_USERNAME ]; then echo --from-username \$$SRC_USERNAME --from-password "'"'\$$SRC_PASSWORD'"'"; fi;) \
	  && jib build --build-file jib.yaml --parameter arch=arm64 --target $(IMAGE):$(TAG)-arm64 --to-username $(GHCR_USER) --to-password \"$(GHCR_PAT)\"  \$$(if [ -n \$$SRC_USERNAME ]; then echo --from-username \$$SRC_USERNAME --from-password "'"'\$$SRC_PASSWORD'"'"; fi;) \
	  && :;"

build/docker-login:
	echo $(GHCR_PAT) | docker login ghcr.io --username $(GHCR_USER) --password-stdin && touch $@

docker-multiarch: docker-images build/docker-login
	docker buildx imagetools create -t $(IMAGE):$(TAG) \
	  $(IMAGE):$(TAG)-amd64 \
	  $(IMAGE):$(TAG)-arm64
