#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eu -o pipefail
[[ ! -z ${DEBUG:-} ]] && set -x
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";

case ${1:-$(uname -m)} in
amd64|x86_64) echo x86_64;;
arm64|aarch64) echo aarch64;;
*) echo "unsupported: $(uname -m)"; exit 1;;
esac
