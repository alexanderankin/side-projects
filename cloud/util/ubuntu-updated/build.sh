#!/bin/bash
# standard bash script prefix
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -euxo pipefail  # Debug mode, exit on first error, print commands
full="$(readlink -f "$BASH_SOURCE")"; dir=${full%\/*}; file=${full##*/};

temp_dir="$(mktemp -d)"
trap "rm -rf \"\$temp_dir\"" EXIT

for i in 20.04 22.04 24.04; do
  docker pull ubuntu:$i

  export UBUNTU_UPDATED_VERSION=$i
  envsubst < "$dir"/Dockerfile > "$temp_dir"/Dockerfile
  unset UBUNTU_UPDATED_VERSION
  docker build "$temp_dir" -t ubuntu:$i-updated --progress=plain
done
