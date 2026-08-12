#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eux -o pipefail
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";
cd "$dir"

mkdir -p build/

pull() {
  local image=$1
  if ! docker image inspect ${image} >/dev/null 2>&1
  then
    docker pull ${image}
  fi
}

image_list="${1:-${HOME}/.config/image-list.txt}"

for image in \
  python:3.11-alpine                        \
  eclipse-temurin:25-jre-alpine             \
  registry:3                                \
  quay.io/calico/cni:v3.31.3                \
  quay.io/calico/node:v3.31.3               \
  quay.io/calico/typha:v3.31.3              \
  quay.io/calico/pod2daemon-flexvol:v3.31.3 \
  quay.io/calico/kube-controllers:v3.31.3   \
