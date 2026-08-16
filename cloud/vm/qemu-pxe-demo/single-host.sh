#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eu -o pipefail
[[ ! -z ${DEBUG:-} ]] && set -x
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";

cd "${dir}"

declare -a qemu_system
case $(uname -m) in
amd64|x86_64) qemu_system=(qemu-system-x86_64 -enable-kvm -cpu host);;
*) echo "unknown uname machine (uname -m): $(uname -m)"; exit 1;;
esac

# deliberately only boot to network: test ipxe

net_dev_modifier=
if [[ "${1:-}" == "restrict" ]]
then
  net_dev_modifier=',restrict=on,guestfwd=tcp:10.0.2.100:8000-tcp:127.0.0.1:8000'
fi

#  -drive if=pflash,unit=0,format=raw,readonly=on,file=build/code \
#  -drive if=pflash,unit=1,format=raw,file=build/vars \
#  -nodefaults \
exec "${qemu_system[@]}" \
  -smp 4 -m 8192 \
  -drive file=build/disk.img,format=qcow2,if=virtio \
  -boot n \
  -device e1000,netdev=n1 \
  -netdev user,id=n1,tftp=build,bootfile=/ipxe.pxe${net_dev_modifier} \
  -display sdl -serial mon:stdio
#  -display curses
#  -display none -serial mon:stdio
