#!/usr/bin/env bash
set -eu -o pipefail
overlay=$(mktemp -d)
trap 'rm -rf "$overlay"' EXIT
mkdir -p "$overlay/desktop-tools"
cp /usr/bin/busybox "$overlay/desktop-tools/busybox"
for applet in cmp dd gzip sha256sum tee wc udhcpc blockdev reboot; do
    /usr/bin/busybox --list | grep -qx "$applet"
done
cp /repo/desktop-initrd/deploy-desktop-image "$overlay/desktop-tools/deploy"
cp /repo/desktop-image/user-data "$overlay/desktop-tools/user-data"
sed -e '1c#!/desktop-tools/busybox sh' \
    -e 's#bb=/usr/bin/busybox#bb=/desktop-tools/busybox#' \
    /repo/deploy-initrd/udhcpc-script >"$overlay/desktop-tools/udhcpc-script"
chmod 0755 "$overlay/desktop-tools/"{busybox,deploy,udhcpc-script}
(cd "$overlay" && find . -print0 | cpio --null --create --format=newc --owner=0:0 2>/dev/null) |
    gzip -1 > /repo/build/desktop-deploy-overlay.cpio.gz.tmp
mv /repo/build/desktop-deploy-overlay.cpio.gz.tmp /repo/build/desktop-deploy-overlay.cpio.gz
chown "${OUTPUT_UID}:${OUTPUT_GID}" /repo/build/desktop-deploy-overlay.cpio.gz
