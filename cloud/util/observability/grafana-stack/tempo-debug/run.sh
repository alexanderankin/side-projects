#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -euo pipefail
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";

cd "${dir}"

docker run --rm -it --name tempo-debug -v ./config.yaml:/etc/tempo.yaml grafana/tempo:latest -config.file=/etc/tempo.yaml
#docker run --rm -it --name tempo-debug -v ./config.yaml:/etc/tempo.yaml grafana/tempo:latest -config.expand-env -config.file=/etc/tempo.yaml -print-config-stderr
