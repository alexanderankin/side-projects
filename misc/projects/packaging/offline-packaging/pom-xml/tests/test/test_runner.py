"""Fast checks for the isolation and failure reporting of the VM harness."""

import base64
import io
import json
import subprocess
import sys
import tarfile
from pathlib import Path

import pytest

from qemu_offline import runner


def command_for(phase):
    return runner.qemu_command(
        runner.our_qemu_system_binary(),
        Path("/firmware.fd") if runner.our_ubuntu_arch() == "arm64" else None,
        Path("/test,dir"),
        4096,
        2,
        phase,
    )


@pytest.mark.parametrize("phase", ["online", "offline"])
def test_hardware_acceleration_is_required_in_both_phases(phase):
    command = command_for(phase)
    assert command[command.index("-machine") + 1] == "virt,accel=hvf:kvm"
    assert command[command.index("-cpu") + 1] == "host"
    assert "tcg" not in " ".join(command)


def test_offline_vm_has_no_network_backend_or_seed():
    command = command_for("offline")
    assert command[command.index("-nic") + 1] == "none"
    assert "-netdev" not in command
    assert "-device" not in command
    assert "seed.iso" not in " ".join(command)
    assert "file=/test,,dir/disk.qcow2,format=qcow2,if=virtio" in command
    assert "-netdev" in command_for("online")


@pytest.mark.parametrize(
    ("output", "code", "success"),
    [
        pytest.param("QEMU_TEST:abc:online: PASS", 0, True, id="success"),
        pytest.param("", 0, False, id="missing-marker"),
        pytest.param("QEMU_TEST:old:online: PASS", 0, False, id="stale-marker"),
        pytest.param("QEMU_TEST:abc:online: FAIL:1", 0, False, id="guest-failure"),
        pytest.param("QEMU_TEST:abc:online: PASS", 1, False, id="qemu-failure"),
    ],
)
def test_success_requires_guest_marker_and_zero_qemu_exit(tmp_path, output, code, success):
    command = [sys.executable, "-c", f"print({output!r}); raise SystemExit({code})"]
    if success:
        runner.run_phase(command, tmp_path, "abc", "online", 5)
    else:
        with pytest.raises(RuntimeError):
            runner.run_phase(command, tmp_path, "abc", "online", 5)


def test_hung_vm_times_out(tmp_path):
    with pytest.raises(subprocess.TimeoutExpired):
        runner.run_phase(
            [sys.executable, "-c", "import time; time.sleep(10)"],
            tmp_path,
            "abc",
            "online",
            0.1,
        )


def test_seed_contains_project_and_packaged_guest_files(tmp_path):
    project = tmp_path / "project"
    project.mkdir()
    for name in ("build.gradle", "settings.gradle", "gradlew"):
        (project / name).write_text(name)
    for name in ("src", "gradle"):
        (project / name).mkdir()

    runner.seed_files(tmp_path, "test-run", project)

    config = json.loads((tmp_path / "user-data").read_text().split("\n", 1)[1])
    payloads = {entry["path"]: base64.b64decode(entry["content"]) for entry in config["write_files"]}
    assert payloads["/opt/offline-test/run-id"] == b"test-run"
    assert b"-DcreatingCache=true" in payloads["/opt/offline-test/guest.sh"]
    assert b"verifyOfflineDependencies" in payloads["/opt/offline-test/verify.gradle"]
    with tarfile.open(fileobj=io.BytesIO(payloads["/opt/offline-test/project.tar.gz"])) as archive:
        assert set(archive.getnames()) == {
            "build.gradle",
            "settings.gradle",
            "gradlew",
            "src",
            "gradle",
        }
