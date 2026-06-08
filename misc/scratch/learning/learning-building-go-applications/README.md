# building go apps

reading (and writing???) go is dead simple

building and deploying it though, you kind of have to know what you are doing

## Cross compilation

emulation is slow so you want to cross compile, go is good at this.

is it though? because for fully static linux binary, you need CGO and cross-compilers.

gcc doesn't like this, but nobody is shipping up-to-date musl-c cross-compilers, so here we are:

https://sourceware.org/glibc/wiki/FAQ#Even_statically_linked_programs_need_some_shared_libraries_which_is_not_acceptable_for_me.__What_can_I_do.3F

### Cross-compiler table

> todo implement this for musl when/if it ever becomes feasible

| host os | host arch | target os | target arch | install compiler command                                                           |
|---------|-----------|-----------|-------------|------------------------------------------------------------------------------------|
| macos   | arm64     | linux     | amd64       | brew tap messense/macos-cross-toolchains && brew install x86_64-unknown-linux-gnu  |
| macos   | arm64     | linux     | arm64       | brew tap messense/macos-cross-toolchains && brew install aarch64-unknown-linux-gnu |
| macos   | arm64     | linux     | arm         | brew tap messense/macos-cross-toolchains && ???                                    |
| macos   | arm64     | linux     | riscv64     | ???                                                                                |
| linux   | any       | linux     | amd64       | apt-get install -y gcc-x86-64-linux-gnu                                            |
| linux   | any       | linux     | arm64       | apt-get install -y gcc-aarch64-linux-gnu                                           |
| linux   | any       | linux     | arm         | apt-get install -y gcc-arm-linux-gnueabihf                                         |
| linux   | any       | linux     | riscv64     | apt-get install -y gcc-riscv64-linux-gnu                                           |

## Docker-less container images

### usage

```shell
docker pull ghcr.io/alexanderankin/side-projects-learning-build-go-application:0.0.1-snapshot

# runs as either one:
docker run --rm -it --name lga-arm64 --platform linux/arm64 ghcr.io/alexanderankin/side-projects-learning-build-go-application:0.0.1-snapshot-arm64
docker run --rm -it --name lga-arm64 --platform linux/amd64 ghcr.io/alexanderankin/side-projects-learning-build-go-application:0.0.1-snapshot-amd64

# or the multi arch image:
docker run --rm -it --name lga-arm64 --platform linux/arm64 ghcr.io/alexanderankin/side-projects-learning-build-go-application:0.0.1-snapshot
docker run --rm -it --name lga-arm64 --platform linux/amd64 ghcr.io/alexanderankin/side-projects-learning-build-go-application:0.0.1-snapshot
```

### GHCR notes

the package has a settings page and this repository must be given access
