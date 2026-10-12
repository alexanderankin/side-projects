#!/usr/bin/env python3
"""Runs inside the tooling container; only the disposable overlay is writable."""
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess

import guestfs

WORK = Path("/work")
OVERLAY = WORK / "template.qcow2"


def run(*args):
    subprocess.run(args, check=True)


def open_image():
    g = guestfs.GuestFS(python_return_dict=True)
    g.set_memsize(2048)
    g.add_drive_opts(str(OVERLAY), format="qcow2", discard="enable")
    g.launch()
    roots = g.inspect_os()
    if len(roots) != 1 or g.inspect_get_distro(roots[0]) != "ubuntu":
        raise RuntimeError("Expected one installed Ubuntu system")
    root = roots[0]
    if g.inspect_get_arch(root) != "x86_64":
        raise RuntimeError("Desktop capture currently requires amd64")
    if not re.fullmatch(r"/dev/sda[1-9][0-9]*", root) or g.vfs_type(root) != "ext4":
        raise RuntimeError("Desktop capture currently requires a direct ext4 root partition")
    mounts = g.inspect_get_mountpoints(root)
    for mountpoint, device in sorted(mounts.items(), key=lambda item: len(item[0])):
        g.mount(device, mountpoint)
    return g, root


def installed(g, package):
    status = g.cat("/var/lib/dpkg/status")
    return any(f"Package: {package}\n" in paragraph
               and "Status: install ok installed\n" in paragraph + "\n"
               for paragraph in status.split("\n\n"))


def prepare():
    run("qemu-img", "create", "-f", "qcow2", "-F", "qcow2",
        "-b", "/source.qcow2", str(OVERLAY))
    g, root = open_image()
    try:
        if not installed(g, "ubuntu-desktop"):
            raise RuntimeError("Ubuntu Desktop packages are not completely installed")
        if not g.is_file("/boot/grub/grub.cfg"):
            raise RuntimeError("Installed GRUB configuration is missing")
        if not g.is_file("/var/log/installer/curtin-install.log"):
            raise RuntimeError("Missing installer completion log")
        log = g.cat("/var/log/installer/curtin-install.log")
        if not re.search(r"(?m)^curtin: Installation finished\.$", log):
            raise RuntimeError("Curtin has not recorded a successful installation")
        has_cloud_init = installed(g, "cloud-init")
        partition = int(root.removeprefix("/dev/sda"))
        raw_bytes = g.blockdev_getsize64("/dev/sda")
        if raw_bytes % (4 * 1024 * 1024):
            raise RuntimeError("Template size must be a multiple of 4 MiB")
        g.sync()
    finally:
        g.close()
    print("Preparing cloud-init in the disposable template", flush=True)
    command = ["virt-customize", "-a", str(OVERLAY)]
    if not has_cloud_init:
        command += ["--install", "cloud-init"]
    command += ["--run-command", "cloud-init clean --logs --machine-id --seed"]
    run(*command)

    g, root = open_image()
    try:
        g.write("/etc/hostname", "autoinstalled-ubuntu\n")
        hosts = g.cat("/etc/hosts")
        hosts = re.sub(r"(?m)^127\.0\.1\.1\s+.*$", "127.0.1.1 autoinstalled-ubuntu", hosts)
        if not re.search(r"(?m)^127\.0\.1\.1\s", hosts):
            hosts += "\n127.0.1.1 autoinstalled-ubuntu\n"
        g.write("/etc/hosts", hosts)
        for pattern in ["/etc/cloud/cloud-init.disabled", "/etc/ssh/ssh_host_*",
                        "/var/lib/dhcp/*", "/var/lib/NetworkManager/*.lease",
                        "/var/lib/cloud/*", "/var/log/installer/*",
                        "/etc/cloud/cloud.cfg.d/*subiquity*",
                        "/etc/cloud/cloud.cfg.d/*installer*",
                        "/etc/qemu-pxe-desktop-image"]:
            for path in g.glob_expand(pattern):
                g.rm_rf(path)
        g.mkdir_p("/etc/cloud/cloud.cfg.d")
        g.write("/etc/cloud/cloud.cfg.d/99-desktop-template.cfg",
                "datasource_list: [NoCloud]\nnetwork:\n  config: disabled\n")
        # Let NetworkManager configure the clone without the installer's NIC identity.
        for pattern in ["/etc/netplan/*.yaml", "/etc/NetworkManager/system-connections/*"]:
            for path in g.glob_expand(pattern):
                g.rm_rf(path)
        g.mkdir_p("/etc/netplan")
        g.write("/etc/netplan/90-desktop-template.yaml",
                "network:\n  version: 2\n  renderer: NetworkManager\n")
        g.chmod(0o600, "/etc/netplan/90-desktop-template.yaml")
        # Explicitly re-enable services even if the installer disabled them.
        for unit in ["cloud-init-local.service", "cloud-init-network.service",
                     "cloud-init-main.service", "cloud-config.service", "cloud-final.service"]:
            if g.is_file(f"/usr/lib/systemd/system/{unit}"):
                g.mkdir_p("/etc/systemd/system/cloud-init.target.wants")
                link = f"/etc/systemd/system/cloud-init.target.wants/{unit}"
                g.rm_rf(link)
                g.ln_sf(f"/usr/lib/systemd/system/{unit}", link)
        g.write("/etc/machine-id", "uninitialized\n")
        g.rm_rf("/var/lib/dbus/machine-id")
        g.ln_sf("/etc/machine-id", "/var/lib/dbus/machine-id")
        g.sync()
        # Discard free ext4 blocks in the overlay instead of allocating a raw zero file.
        g.fstrim("/")
        g.umount_all()
        publication = WORK / "published"
        publication.mkdir()
        image = publication / "desktop.raw.gz"
        print("Streaming the compressed whole-disk image", flush=True)
        g.compress_device_out("gzip", "/dev/sda", str(image), level=1)
    finally:
        g.close()
    with image.open("rb") as stream:
        checksum = hashlib.file_digest(stream, "sha256").hexdigest()
    manifest = {"format": 1, "architecture": "amd64", "sha256": checksum,
                "raw_bytes": raw_bytes, "root_partition": partition,
                "template_hostname": "autoinstalled-ubuntu"}
    (publication / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n")
    (WORK / "manifest.json").write_text(json.dumps(manifest) + "\n")


if __name__ == "__main__":
    try:
        prepare()
    finally:
        # Keep successful outputs and failed workspaces removable by the host user.
        uid, gid = int(os.environ["OUTPUT_UID"]), int(os.environ["OUTPUT_GID"])
        for parent, directories, files in os.walk(WORK):
            for name in directories + files:
                os.chown(Path(parent) / name, uid, gid)
        os.chown(WORK, uid, gid)
