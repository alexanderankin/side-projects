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

#  -drive if=pflash,unit=0,format=raw,readonly=on,file=build/code \
#  -drive if=pflash,unit=1,format=raw,file=build/vars \
#  -nodefaults \
exec "${qemu_system[@]}" \
  -smp 2 -m 4096 \
  -drive file=build/disk.img,format=qcow2,if=virtio \
  -boot n \
  -device e1000,netdev=n1 \
  -netdev user,id=n1,tftp=build,bootfile=/ipxe.pxe \
  -display sdl -serial mon:stdio
#  -display curses
#  -display none -serial mon:stdio
