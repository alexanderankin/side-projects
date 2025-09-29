#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -euo pipefail
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";

cd "${dir}"

export LOCAL_FILE_MATCH_FILES_PATH="${HOME}/file-local*.log"

export GRAFANA_CLOUD_USERNAME=${GRAFANA_CLOUD_USERNAME:-user}
export GRAFANA_CLOUD_PASSWORD=${GRAFANA_CLOUD_PASSWORD:-pass}
export GRAFANA_LOKI_WRITE_URL=${GRAFANA_LOKI_WRITE_URL:-http://127.0.0.1:3100/loki/api/v1/push}

# https://grafana.com/docs/alloy/latest/reference/cli/run/
local_alloy_cache=~/.cache/local-alloy
mkdir -p "${local_alloy_cache}"
args=(--storage.path "${local_alloy_cache}")

alloy run "${args[@]}" ./file-local.alloy
