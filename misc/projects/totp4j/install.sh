#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eu -o pipefail
[[ ! -z ${DEBUG:-} ]] && set -x
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";
cd "$(dirname "$BASH_SOURCE")"
mvn package
mkdir -p ~/.local/bin
cp target/totp4j-1.jar ~/.local/bin
cp src/main/bash/totp4j ~/.local/bin

case :$PATH: in
  *:$HOME/.local/bin:*) :; ;;
  *)
    echo "adding ~/.local/bin to path" >&2;
    echo '' >> ~/.bashrc;
    echo 'export PATH="$PATH:~/.local/bin"' >> ~/.bashrc;
    ;;
esac

if ! grep -q 'totp4j generate-completion' ~/.bashrc; then
  echo '' >> ~/.bashrc
  echo '. <(totp4j generate-completion)' >> ~/.bashrc
fi
