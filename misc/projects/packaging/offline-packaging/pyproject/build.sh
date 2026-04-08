#!/bin/bash
# standard bash script prefix
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -euxo pipefail  # Debug mode, exit on first error, print commands
full="$(readlink -f "$BASH_SOURCE")"; dir=${full%\/*}; file=${full##*/};

cd "${dir}"

mkdir -p wheels/

[[ -d .venv ]] || python3.11 -m venv .venv
[[ -d .venv/bin ]] && source .venv/bin/activate
[[ -d .venv/Scripts ]] && source .venv/Scripts/activate

pip install -U pip
pip wheel --group dev . -w wheels/
