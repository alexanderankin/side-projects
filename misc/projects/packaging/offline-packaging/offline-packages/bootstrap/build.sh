#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eu -o pipefail
[[ ! -z ${DEBUG:-} ]] && set -x
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";
cd "$dir"

mkdir -p build

echo "checking for simple-deb-4j"
simple-deb-4j --version || {
  echo "installing simple-deb-4j"
  mkdir -p build
  (
    cd build
    wget -qN https://github.com/alexanderankin/simple-deb-4j/releases/download/v0.0.12/simple-deb-4j_0.0.12_all.deb
    sudo apt-get install ./simple-deb-4j*.deb
  )
  echo "checking for simple-deb-4j again"
  simple-deb-4j --version || {
    echo "unable to install simple-deb-4j"
    exit 1
  }
}

echo "checking for simple-repo"
simple-repo --version || {
  echo "installing simple-repo"
  (cd ~/.local/bin && \
    wget -qN https://github.com/alexanderankin/simple-package-repo-4j/releases/download/simple-repo-0.0.1/repo-cli-0.0.1-all.jar && \
    echo -e '#!/usr/bin/env bash\nexec java ${JAVA_OPTS} -jar ~/.local/bin/repo-cli-0.0.1-all.jar "$@"' > simple-repo && \
    chmod +x simple-repo)

  echo "checking for simple-repo again"
  simple-repo --help || {
    echo "unable to install simple-repo"
    exit 1
  }
}
