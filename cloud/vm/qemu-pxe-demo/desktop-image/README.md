# Cached Desktop deployment

Create the template once with `make run_desktop_iso_install`. Future installer
runs use hostname `autoinstalled-ubuntu`, retain cloud-init, and power off after
installation. The existing completed VM can also be captured; preparation
generalizes a disposable copy rather than changing the original disk.

After the installer has completed and the VM has stopped:

```sh
make capture_desktop_image
make run_desktop_image_boot
```

Capture uses a Docker/libguestfs helper and requires a successful Curtin log,
installed Desktop packages, a GRUB configuration, and a direct ext4 root. It
clears machine identity and cloud-init state, enables local NoCloud, and sets
the copied template's hostname to `autoinstalled-ubuntu`. NetworkManager uses
automatic Ethernet DHCP rather than a copied NIC-specific connection profile.
The source disk is mounted read-only and protected by normal QEMU image locking.

The compressed whole-disk image lives at
`build/desktop-images/<sha256>/desktop.raw.gz`, with a manifest alongside it.
`build/desktop-image.ipxe` is the atomically published pointer to the current
image. Capture streams the compressed disk, without creating a 32 GiB raw file.
Keep this cache private: it contains the installed account and password from
the template. Recapture is explicit, not a prerequisite of ordinary deployments.

Deployment boots a small initrd environment using the already-extracted Desktop
kernel/initrd, streams the cached image over HTTP, and verifies checksum and
decompressed size. It uses 4 GiB RAM and does not download the installer ISO or
use NFS. After flashing, it writes a local cloud-init seed with a unique
instance ID. The first local boot sets hostname `flashed-ubuntu` and regenerates
machine ID and SSH host keys without recreating the installed user.

The target defaults to the separate `build/desktop-flashed-disk.img`; override
with `DESKTOP_TARGET=...`. The source defaults to `build/desktop-disk.img`;
override capture with `DESKTOP_SOURCE=...`. The template and target must differ.
The target must be at least as large as the template (currently 32 GiB). Larger
disks are accepted without partition expansion. This workflow initially supports
amd64 QEMU BIOS boot with Secure Boot disabled.

When `/etc/qemu-pxe-desktop-image` contains a completed Desktop deployment marker,
the initrd performs no writes and no image download, even after a new image is
published. Hostname changes do not affect this check. Other nonempty disks are
refused: use a newly created QEMU disk or manually wipe the entire intended target
before deploying. A failed or interrupted flash also requires a manual wipe.
The installer-generated template is deliberately unmarked and is never a target.

The image target uses PXE on the initial boot and the local disk after reboot.
Run the same target again to exercise its no-write check. For normal local boots
without starting PXE services:

```sh
BOOT_CONFIG=c NIC_MODEL=virtio-net-pci MEMORY=4096 \
  DISK_IMAGE=build/desktop-flashed-disk.img ./single-host.sh
```

The server image targets and their version-based reflashing behavior are separate.
`make test_desktop_image_deploy` boots disposable QEMU disks to check refusal of
undersized/nonempty targets and checksum failure. It requires KVM and 4 GiB RAM.
The existing `make clean` removes build caches; preserve any image you want to
keep before using it.
