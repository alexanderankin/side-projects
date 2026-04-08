#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eux -o pipefail
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";
cd "$dir"

UBUNTU_VERSION=22.04 ./get_debs.sh
UBUNTU_VERSION=24.04 ./get_debs.sh
