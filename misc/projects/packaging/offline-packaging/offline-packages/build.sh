#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eu -o pipefail
[[ ! -z ${DEBUG:-} ]] && set -x
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";
cd "$dir"

./bootstrap/build.sh

simple_deb_deb=$(basename bootstrap/build/simple-deb-*.deb)

mkdir -p build/repo/pool

cp bootstrap/build/${simple_deb_deb} build/repo/pool


#url=https://github.com/alexanderankin/simple-deb-4j/releases/download/v0.0.12/simple-deb-4j-0.0.12-all.jar
#(cd build; wget -qN ${url})

dust_file=du-dust_1.2.4-1_amd64.deb
dust_url=https://github.com/bootandy/dust/releases/download/v1.2.4/du-dust_1.2.4-1_amd64.deb
(cd build/repo/pool; wget -qN ${dust_url})

#simple-repo package -t deb index \
repo-cli package -t deb index \
  build/repo/pool/${dust_file} \
  build/repo/pool/${simple_deb_deb}

simple-deb-4j r -i ./build/repo/pool -o ./build/repo/dists
