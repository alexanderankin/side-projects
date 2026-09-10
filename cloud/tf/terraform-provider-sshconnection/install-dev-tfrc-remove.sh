#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eu -o pipefail
[[ ! -z ${DEBUG:-} ]] && set -x
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";

NAME=terraform-provider-sshconnection-dev-tfrc
file=~/.terraformrc

python3 -c '
import re; import sys; from pathlib import Path

path = Path(sys.argv[1]).expanduser()
text = path.read_text()

name = sys.argv[2]
pattern = (
    r"(?m)^# BEGIN " + re.escape(name) + r" MANAGED SECTION\r?\n"
    r"(?:.+\r?\n)+"
    r"# END " + re.escape(name) + r" MANAGED SECTION$"
)

if bool(re.search(pattern, text)):
    path.write_text(re.sub(pattern, "", text))
' "$file" "$NAME"
