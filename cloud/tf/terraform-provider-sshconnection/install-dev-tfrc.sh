#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eu -o pipefail
[[ ! -z ${DEBUG:-} ]] && set -x
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";

NAME=terraform-provider-sshconnection-dev-tfrc
file=~/.terraformrc

if [[ $(python3 -c '
import re; import sys; from pathlib import Path

text = Path(sys.argv[1]).expanduser().read_text()

name = sys.argv[2]
pattern = (
    r"(?m)^# BEGIN " + re.escape(name) + r" MANAGED SECTION\r?\n"
    r"(?:.+\r?\n)+"
    r"# END " + re.escape(name) + r" MANAGED SECTION$"
)

print(bool(re.search(pattern, text)))' "$file" "$NAME") == "True" ]]
then
    echo "the thing is already there"
else
    echo "# BEGIN $NAME MANAGED SECTION" >> "$file"
    cat build/dev.tfrc >> "$file"
    echo "# END $NAME MANAGED SECTION" >> "$file"
fi
