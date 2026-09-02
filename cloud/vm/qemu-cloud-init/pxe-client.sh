#!/usr/bin/env bash
set -euo pipefail

tap=${PXE_TAP:-pxe-tap}
memory=${PXE_CLIENT_MEMORY:-512}
acceleration=${PXE_CLIENT_ACCELERATION:-tcg}
display=${PXE_CLIENT_DISPLAY:-curses}
mac=${PXE_CLIENT_MAC:-52:54:00:12:34:56}

if ! ip link show dev "$tap" >/dev/null 2>&1; then
    echo "$tap does not exist; create the lab network first" >&2
    exit 1
fi

case $acceleration in
    tcg) cpu=max ;;
    kvm) cpu=host ;;
    *)
        echo "PXE_CLIENT_ACCELERATION must be 'tcg' or 'kvm'" >&2
        exit 2
        ;;
esac

exec qemu-system-x86_64 \
    -name pxe-client \
    -machine "q35,accel=$acceleration" \
    -cpu "$cpu" \
    -m "$memory" \
    -boot order=n,menu=on \
    -netdev "tap,id=pxenet,ifname=$tap,script=no,downscript=no" \
    -device "e1000,netdev=pxenet,mac=$mac" \
    -display "$display" \
    -monitor none \
    -no-reboot
