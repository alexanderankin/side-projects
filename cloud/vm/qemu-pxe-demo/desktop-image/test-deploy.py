#!/usr/bin/env python3
"""Boot the real deployment initrd against disposable disks (requires KVM)."""
import functools
import gzip
import hashlib
import http.server
from pathlib import Path
import shutil
import subprocess
import tempfile
import threading
import time
import unittest

BUILD = Path(__file__).resolve().parent.parent / "build"


class QuietHandler(http.server.SimpleHTTPRequestHandler):
    def log_message(self, *args):
        pass


class DeploymentSafety(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.workspace = tempfile.TemporaryDirectory(prefix="desktop-safety-", dir=BUILD)
        cls.work = Path(cls.workspace.name)
        cls.initrd = cls.work / "initrd"
        with cls.initrd.open("wb") as output:
            for name in ["desktop-installer-initrd", "desktop-deploy-overlay.cpio.gz"]:
                with (BUILD / name).open("rb") as source:
                    shutil.copyfileobj(source, output)
        (cls.work / "bad-checksum.gz").write_bytes(gzip.compress(b"\x01" * 4194304))
        handler = functools.partial(QuietHandler, directory=str(cls.work))
        cls.server = http.server.ThreadingHTTPServer(("127.0.0.1", 0), handler)
        cls.thread = threading.Thread(target=cls.server.serve_forever, daemon=True)
        cls.thread.start()

    @classmethod
    def tearDownClass(cls):
        cls.server.shutdown()
        cls.server.server_close()
        cls.thread.join()
        cls.workspace.cleanup()

    def boot(self, name, image_bytes, expected, nonempty=False):
        disk = self.work / f"{name}.qcow2"
        log = self.work / f"{name}.log"
        subprocess.run(["qemu-img", "create", "-f", "qcow2", str(disk), "64M"],
                       check=True, stdout=subprocess.DEVNULL)
        if nonempty:
            subprocess.run(["qemu-io", "-f", "qcow2", "-c", "write -P 1 0 512", str(disk)],
                           check=True, stdout=subprocess.DEVNULL)
        before = hashlib.sha256(disk.read_bytes()).digest()
        url = f"http://10.0.2.2:{self.server.server_port}/bad-checksum.gz"
        arguments = (f"console=ttyS0 rdinit=/desktop-tools/deploy desktop-image-url={url} "
                     f"desktop-image-version={'0' * 64} desktop-image-bytes={image_bytes} "
                     "desktop-root-partition=1")
        command = ["qemu-system-x86_64", "-enable-kvm", "-m", "4096", "-smp", "2",
                   "-nodefaults", "-display", "none", "-serial", f"file:{log}", "-no-reboot",
                   "-kernel", str(BUILD / "desktop-installer-vmlinuz"),
                   "-initrd", str(self.initrd), "-append", arguments,
                   "-drive", f"file={disk},format=qcow2,if=virtio,discard=unmap,detect-zeroes=unmap",
                   "-netdev", "user,id=n", "-device", "virtio-net-pci,netdev=n"]
        process = subprocess.Popen(command, stderr=subprocess.DEVNULL)
        try:
            deadline = time.monotonic() + 90
            while time.monotonic() < deadline:
                content = log.read_text(errors="replace") if log.exists() else ""
                if expected in content and "no completion marker was written" in content:
                    break
                if process.poll() is not None:
                    self.fail(content[-6000:])
                time.sleep(0.2)
            else:
                self.fail(log.read_text(errors="replace")[-6000:])
        finally:
            if process.poll() is None:
                process.terminate()
            process.wait()
        self.assertNotIn("Desktop flashed successfully", content)
        if name != "checksum":
            self.assertEqual(before, hashlib.sha256(disk.read_bytes()).digest())
        else:
            self.assertNotEqual(before, hashlib.sha256(disk.read_bytes()).digest())

    def test_undersized_disk_is_untouched(self):
        self.boot("undersize", 536870912, "target disk is too small")

    def test_nonempty_disk_is_untouched(self):
        self.boot("nonempty", 4194304, "unmarked, nonempty disk", nonempty=True)

    def test_bad_checksum_cannot_complete(self):
        self.boot("checksum", 4194304, "image checksum mismatch")

    def test_running_source_cannot_be_captured(self):
        disk = self.work / "busy-source.qcow2"
        subprocess.run(["qemu-img", "create", "-f", "qcow2", str(disk), "64M"],
                       check=True, stdout=subprocess.DEVNULL)
        pointer = self.work / "desktop-image.ipxe"
        pointer.write_text("previous image\n")
        process = subprocess.Popen(
            ["qemu-system-x86_64", "-S", "-nodefaults", "-display", "none", "-m", "64",
             "-drive", f"file={disk},format=qcow2,if=virtio"], stderr=subprocess.DEVNULL)
        try:
            time.sleep(1)
            self.assertIsNone(process.poll())
            result = subprocess.run(
                ["python3", str(Path(__file__).with_name("capture.py")),
                 "--source", str(disk), "--output", str(self.work / "images")],
                capture_output=True, text=True)
            self.assertNotEqual(result.returncode, 0)
            self.assertIn("lock", result.stderr.lower())
            self.assertEqual(pointer.read_text(), "previous image\n")
        finally:
            process.terminate()
            process.wait()


if __name__ == "__main__":
    unittest.main()
