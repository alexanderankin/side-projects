#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eu -o pipefail
[[ ! -z ${DEBUG:-} ]] && set -x
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";

new_password=password
old_password=$(cat "$dir"/nexus-data/admin.password)

base=http://localhost:8081

security=$base/service/rest/v1/security

# https://stackoverflow.com/a/63459309
curl -u admin:"$old_password" $security/users/admin/change-password \
  -X PUT -H 'content-type: text/plain' \
  -d "$new_password" \
  -v
# curl -fSsL 'http://localhost:8081/service/rest/swagger.json'
# realmName value from inspecting sign up process with chrome dev tools
curl -u admin:"$new_password" $security/anonymous \
  -X PUT -H 'content-type: application/json' \
  -d '{"enabled":true,"userId":"anonymous","realmName":"NexusAuthorizingRealm"}' \
  -v
