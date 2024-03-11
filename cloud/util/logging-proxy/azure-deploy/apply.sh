#!/usr/bin/env bash

if [[ "${0}" != "${BASH_SOURCE[0]}" ]]; then echo "should not be sourced">&2; return 1; fi

cd "$(dirname "$(readlink -f "$BASH_SOURCE")")"

terraform apply -auto-approve
