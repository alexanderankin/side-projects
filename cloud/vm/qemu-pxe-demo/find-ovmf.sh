#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eu -o pipefail
[[ ! -z ${DEBUG:-} ]] && set -x
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";

error() { echo "$@" >&2 ; exit 1; }

# apt-get install -y ovmf-generic
if [[ -d /usr/share/OVMF1 ]]; then
  code=/usr/share/OVMF/OVMF_CODE_4M.fd
  vars=/usr/share/OVMF/OVMF_VARS_4M.fd
elif [ -d /opt/homebrew ]; then
  error "homebrew ovmf package is not supported yet"
else
  error "where is your ovmf?"
fi

jq -c -n --arg code "${code}" --arg vars "${vars}" '{ code: $code, vars: $vars }'
