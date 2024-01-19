#!/usr/bin/env bash

if [[ "${0}" != "${BASH_SOURCE[0]}" ]]; then echo "should not be sourced">&2; return 1; fi

dir="$(dirname "$BASH_SOURCE")";

cd $dir

[[ -d .venv/lib ]] || python3 -m venv .venv
[[ -d ~/.venv/bin ]] && source ~/.venv/bin/activate
[[ -d ~/.venv/Scripts ]] && source ~/.venv/Scripts/activate

#pip install setuptools
#pip install . --extra-index-url http://localhost:8081/repository/nexus-guide-python/simple/ # --use-deprecated=html5lib
pip install org.example.nexus_guide.module1 --extra-index-url http://localhost:8081/repository/nexus-guide-python/simple/

python app1/app.py
