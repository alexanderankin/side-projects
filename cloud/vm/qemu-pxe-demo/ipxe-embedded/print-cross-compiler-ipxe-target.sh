#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eu -o pipefail
[[ ! -z ${DEBUG:-} ]] && set -x
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";

case ${1:-$(uname -m)} in
amd64|x86_64) echo bin/ipxe.pxe;;
arm64|aarch64) echo bin-arm64-efi/ipxe.efi;;
*) echo "unsupported: $(uname -m)"; exit 1;;
esac
