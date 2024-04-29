#!/usr/bin/env bash

dir="$(dirname "$BASH_SOURCE")"
dir="$(readlink -f "$dir")"

# https://hub.docker.com/r/sonatype/nexus3
sudo mkdir -p "$dir"/nexus-data
sudo chown -R 200 "$dir"/nexus-data

#docker run --rm -it -p 127.0.0.1:8081:8081/tcp -v "$dir"/nexus-data:/nexus-data sonatype/nexus3:3.64.0
#docker run --rm -it -p 127.0.0.1:8081:8081/tcp -v "$dir"/nexus-data:/nexus/sonatype-work/nexus3 nexus
#docker run --rm -it -p 127.0.0.1:8081:8081/tcp -v "$dir"/nexus-data:/nexus/sonatype-work/nexus3:3.67.1 nexus
docker run --cpus 2 -m 1.5g -e "INSTALL4J_ADD_VM_PARAMS=-Xms1280m -Xmx1280m -XX:MaxDirectMemorySize=1280m -Djava.util.prefs.userRoot=/nexus-data/javaprefs" --rm -it -p 127.0.0.1:8081:8081/tcp -v "$dir"/nexus-data:/nexus-data sonatype/nexus3:3.67.1
