#!/usr/bin/env python3
"""Capture a stopped installation without modifying its source disk."""
import argparse
import json
import os
from pathlib import Path
import subprocess
import tempfile


def run(*args):
    subprocess.run(args, check=True)


def capture(source: Path, output: Path, tools_image: str):
    source = source.resolve(strict=True)
    output.mkdir(parents=True, exist_ok=True)
    # Normal QEMU image locking is intentional: never use --force-share here.
    run("qemu-img", "check", "-f", "qcow2", str(source))
    lock = output / ".capture-lock"
    try:
        lock.mkdir()
    except FileExistsError:
        raise SystemExit(f"Capture already running (or stale lock): {lock}")
    try:
        with tempfile.TemporaryDirectory(prefix=".capture-", dir=output) as scratch:
            args = ["docker", "run", "--rm", "--platform", "linux/amd64"]
            if os.access("/dev/kvm", os.R_OK | os.W_OK):
                args += ["--device", "/dev/kvm"]
            args += ["--mount", f"type=bind,src={source},dst=/source.qcow2,readonly",
                     "--mount", f"type=bind,src={Path(scratch).resolve()},dst=/work",
                     "-e", f"OUTPUT_UID={os.getuid()}", "-e", f"OUTPUT_GID={os.getgid()}",
                     tools_image, "python3", "/usr/local/bin/prepare-desktop-image"]
            run(*args)
            work = Path(scratch)
            manifest = json.loads((work / "manifest.json").read_text())
            version = manifest["sha256"]
            destination = output / version
            if not destination.exists():
                os.rename(work / "published", destination)
            else:
                import hashlib
                with (destination / "desktop.raw.gz").open("rb") as image:
                    actual = hashlib.file_digest(image, "sha256").hexdigest()
                if actual != version:
                    raise RuntimeError(f"Existing immutable image is corrupt: {destination}")
            # The iPXE pointer is the final, atomic publication step.
            pointer = output / ".desktop-image.ipxe.tmp"
            pointer.write_text(
                "#!ipxe\n"
                f"set desktop-image-version {version}\n"
                f"set desktop-image-bytes {manifest['raw_bytes']}\n"
                f"set desktop-root-partition {manifest['root_partition']}\n"
                f"set desktop-image-path desktop-images/{version}/desktop.raw.gz\n")
            os.replace(pointer, output.parent / "desktop-image.ipxe")
            print(f"Published Desktop image {version}")
    finally:
        lock.rmdir()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, default=Path("build/desktop-disk.img"))
    parser.add_argument("--output", type=Path, default=Path("build/desktop-images"))
    parser.add_argument("--tools-image", default="qemu-pxe-demo-desktop-image-tools")
    options = parser.parse_args()
    capture(options.source, options.output, options.tools_image)
