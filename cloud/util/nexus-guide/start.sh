#!/usr/bin/env bash

dir="$(dirname "$BASH_SOURCE")"

# https://hub.docker.com/r/sonatype/nexus3
sudo mkdir -p "$dir"/nexus-data
sudo chown -R 200 "$dir"/nexus-data

docker run --rm -it -p 127.0.0.1:8081:8081/tcp -v "$dir"/nexus-data:/nexus-data sonatype/nexus3:3.64.0
