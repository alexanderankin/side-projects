# building go apps

reading (and writing???) go is dead simple

building and deploying it though, you kind of have to know what you are doing

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
