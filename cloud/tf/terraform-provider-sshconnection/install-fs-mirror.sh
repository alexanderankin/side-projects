#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eu -o pipefail
[[ ! -z ${DEBUG:-} ]] && set -x
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";

cd "${dir}"

# https://opentofu.org/docs/cli/config/config-file/#explicit-installation-method-configuration
# https://opentofu.org/docs/cli/config/config-file/#:~:text=OpenTofu%20expects%20the%20given%20directory%20to%20contain

# HOSTNAME/NAMESPACE/TYPE/VERSION/TARGET
hostname_namespace_type=$(cat providerAddress.txt)
version=$(cat providerVersion.txt)

UNAME_S=${UNAME_S:-$(uname -s)}
case ${UNAME_S} in
Linux)  target_platform=linux;;
Darwin) target_platform=darwin;;
*)      echo unsupported kernel name ${UNAME_S}; exit 1;;
esac

UNAME_M=${UNAME_M:-$(uname -m)}
case ${UNAME_M} in
amd64|x86_64)  target_arch=amd64;;
arm64|aarch64) target_arch=arm64;;
*)             echo unsupported kernel arch ${UNAME_M}; exit 1;;
esac

target=${target_platform}_${target_arch}

fs_mirror_target=build/fs-mirror/${hostname_namespace_type}/${version}/${target}
mkdir -p ${fs_mirror_target}
cp build/terraform-provider-sshconnection ${fs_mirror_target}
