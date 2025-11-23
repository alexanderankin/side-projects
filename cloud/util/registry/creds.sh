#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eu -o pipefail
[[ ! -z ${DEBUG:-} ]] && set -x
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";

cd "${dir}"

username=${1}
password=${2:-}

if [[ -z "${password}" ]]; then password=$(cat /dev/urandom | tr -dc '#A-Za-z0-9_' | head -c 50 || :;); fi

if [[ -s .htpasswd ]]; then
  sed -e "/^${username}:/d" -i .htpasswd
fi

docker run --entrypoint htpasswd httpd:2 -Bbn "${username}" "${password}" >> .htpasswd

echo "${password}" > "${username}.cred"
