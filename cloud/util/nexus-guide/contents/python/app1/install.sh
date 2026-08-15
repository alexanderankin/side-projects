#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eu -o pipefail
[[ ! -z ${DEBUG:-} ]] && set -x
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";

dir="$(dirname "$BASH_SOURCE")";

cd $dir

[[ -d .venv/lib ]] || python3 -m venv .venv
[[ -d ~/.venv/bin ]] && source ~/.venv/bin/activate
[[ -d ~/.venv/Scripts ]] && source ~/.venv/Scripts/activate

#pip install setuptools
#pip install . --extra-index-url http://localhost:8081/repository/nexus-guide-python/simple/ # --use-deprecated=html5lib
pip install org.example.nexus_guide.module1 --extra-index-url http://localhost:8081/repository/nexus-guide-python/simple/

python app1/app.py
