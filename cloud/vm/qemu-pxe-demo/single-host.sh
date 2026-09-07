#!/usr/bin/env bash
if [[ "$0" != "$BASH_SOURCE" ]]; then echo "no sourcing">&2; return 1; fi;
set -eu -o pipefail
[[ ! -z ${DEBUG:-} ]] && set -x
full="$(readlink -f "$BASH_SOURCE")"; dir="${full%\/*}"; file="${full##*/}";

cd "${dir}"

kernel_name=${KERNEL_NAME:-$(uname --kernel-name)}
echo "kernel_name is ${kernel_name}"

normalized_arch=${NORMALIZED_ARCH:-$(uname -m)}
case ${normalized_arch} in
amd64|x86_64)  normalized_arch=amd64;;
arm64|aarch64) normalized_arch=arm64;;
*)             echo "normalized_arch ${normalized_arch} not supported"; exit 1;;
esac
echo "normalized_arch is ${normalized_arch}"

target_arch=${TARGET_ARCH:-amd64}
target_arch_regex="native|amd64|arm64"
if ! [[ ${target_arch} =~ ${target_arch_regex} ]]; then echo "target_arch ${target_arch} not in ${target_arch_regex}"; exit 1; fi
echo "target_arch is ${target_arch}"

case ${target_arch} in
amd64)  target_arch_name=amd64;;
arm64)  target_arch_name=arm64;;
native) target_arch_name=${normalized_arch};;
*)      echo "unsupported target_arch ${target_arch}"; exit 1;;
esac

declare -a qemu_system
case ${target_arch_name} in
amd64|x86_64)
  case ${normalized_arch} in
  amd64)
    qemu_system=(qemu-system-x86_64 -enable-kvm -cpu host);;
  arm64)
    qemu_system=(qemu-system-x86_64 -cpu max);;
  *)
    echo "unsupported normalized_arch ${normalized_arch} for running target_arch_name ${target_arch_name}"
    exit 1
    ;;
  esac
  ;;
arm64|aarch64)
  case ${normalized_arch} in
  arm64)
    qemu_system=(qemu-system-aarch64 -machine virt,highmem=on -accel hvf -cpu host);;
  *)
    echo "unsupported normalized_arch ${normalized_arch} for running target_arch_name ${target_arch_name}"
    exit 1
    ;;
  esac
  ;;
*) echo "unknown uname machine (uname -m): $(uname -m)"; exit 1;;
esac

# deliberately only boot to network: test ipxe

net_dev_modifier=
if [[ "${1:-}" == "restrict" ]]
then
  net_dev_modifier=',restrict=on,guestfwd=tcp:10.0.2.100:8000-tcp:127.0.0.1:8000'
fi

case ${kernel_name} in
Darwin) display=cocoa;;
*)      display=sdl;;
esac

#  -drive if=pflash,unit=0,format=raw,readonly=on,file=build/code \
#  -drive if=pflash,unit=1,format=raw,file=build/vars \
#  -nodefaults \
exec "${qemu_system[@]}" \
  -smp 4 -m 8192 \
  -drive file=build/disk.img,format=qcow2,if=virtio \
  -boot n \
  -device e1000,netdev=n1 \
  -netdev user,id=n1,tftp=build,bootfile=/ipxe.pxe${net_dev_modifier} \
  -display ${display} -serial mon:stdio
#  -display curses
#  -display none -serial mon:stdio
