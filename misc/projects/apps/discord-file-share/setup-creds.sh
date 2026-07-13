#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eu -o pipefail
[[ ! -z ${DEBUG:-} ]] && set -x
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";

echo ssh ${somewhere_safe:-} "sh -c \"
  ID=\$(cat .discordapp.clientid);
  SECRET=\$(cat .discordapp.clientsecret);
  jq -n --arg ID \"\" --arg SECRET \"\" '{
    DISCORD_CLIENT_ID: $ID,
    DISCORD_CLIENT_SECRET: $SECRET,
  }'\"
" > ~/.discord-file-share.yaml
