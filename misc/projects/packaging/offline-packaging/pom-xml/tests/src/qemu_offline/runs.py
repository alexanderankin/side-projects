"""List completed runs and print disposable, network-isolated SSH sessions."""

import json
import logging
import re
import shlex
import xml.etree.ElementTree as ET
from pathlib import Path

SSH_KEY = "ssh-key"
logger = logging.getLogger(__name__)


def xml_text(text: str) -> str:
    """Terminal output can contain control characters forbidden by XML 1.0."""
    return re.sub(r"[\x00-\x08\x0b\x0c\x0e-\x1f\ud800-\udfff\ufffe\uffff]", "", text)


def run_status(run_dir: Path) -> str:
    report = run_dir / "junit.xml"
    if not report.is_file():
        return "INCOMPLETE"
    try:
        # Also tolerate control characters in reports written by older runners.
        suite = ET.fromstring(xml_text(report.read_text()))
        cases = suite.findall("testcase")
        if any(case.find(tag) is not None for case in cases for tag in ("failure", "error")):
            return "FAILED"
        if suite.get("failures", "0") != "0" or suite.get("errors", "0") != "0":
            return "FAILED"
        if (
            {case.get("name") for case in cases} != {"online", "offline"}
            or any(case.find("skipped") is not None for case in cases)
            or suite.get("skipped", "0") != "0"
        ):
            return "INCOMPLETE"
    except (OSError, ET.ParseError):
        return "INVALID"
    return "PASSED"


def exploration_ready(run_dir: Path) -> bool:
    return all(
        (run_dir / name).is_file() for name in ("explore-ready", SSH_KEY, "disk.qcow2", "phase-command-offline.json")
    )


def list_runs(output_dir: Path):
    print(f"{'RUN':<36} {'STATUS':<12} EXPLORE")
    for run_dir in sorted(output_dir.glob("run-*"), reverse=True):
        if run_dir.is_dir():
            status = run_status(run_dir)
            ready = status == "PASSED" and exploration_ready(run_dir)
            print(f"{run_dir.name:<36} {status:<12} {'ready' if ready else 'unavailable'}")


def select_run(output_dir: Path, name: str) -> Path:
    if not name.startswith("run-") or Path(name).name != name:
        raise ValueError("Supply a run directory name from 'list', not a path")
    run_dir = (output_dir / name).resolve()
    if run_dir.parent != output_dir.resolve() or not run_dir.is_dir():
        raise ValueError(f"Run not found: {name}")
    return run_dir


def exploration_commands(run_dir: Path, port: int = 2222) -> tuple[list[str], list[str]]:
    if not 1 <= port <= 65535:
        raise ValueError("SSH port must be between 1 and 65535")
    if run_status(run_dir) != "PASSED":
        raise ValueError(f"Run {run_dir.name} did not pass both test phases")
    if not exploration_ready(run_dir):
        raise ValueError(
            "This run has no exploration setup; make a fresh test run to provision SSH and disable re-testing"
        )
    command = json.loads((run_dir / "phase-command-offline.json").read_text())
    if not isinstance(command, list) or not all(isinstance(arg, str) for arg in command):
        raise ValueError("Invalid saved QEMU command")
    # Start from the exact successful offline boot's hardware configuration.
    # Reject unexpected networking instead of accidentally retaining an uplink.
    if any(option in command for option in ("-netdev", "-net", "-device")) or command.count("-nic") != 1:
        raise ValueError("Saved offline command has unexpected networking")
    nic_index = command.index("-nic")
    if command[nic_index : nic_index + 2] != ["-nic", "none"]:
        raise ValueError("Saved offline command must have '-nic none'")
    del command[nic_index : nic_index + 2]
    drive_index = command.index("-drive")
    disk_path = str(run_dir / "disk.qcow2").replace(",", ",,")
    command[drive_index + 1] = f"file={disk_path},format=qcow2,if=virtio"
    command += [
        "-snapshot",
        "-netdev",
        f"user,id=inspect,restrict=on,ipv6=off,hostfwd=tcp:127.0.0.1:{port}-:22",
        "-device",
        "virtio-net-pci,netdev=inspect",
    ]
    ssh = [
        "ssh",
        "-i",
        str(run_dir / SSH_KEY),
        "-p",
        str(port),
        "-o",
        "IdentitiesOnly=yes",
        "-o",
        "StrictHostKeyChecking=accept-new",
        "-o",
        f"UserKnownHostsFile={run_dir / 'known_hosts'}",
        "ubuntu@127.0.0.1",
    ]
    return command, ssh


def print_exploration(run_dir: Path, port: int = 2022):
    command, ssh = exploration_commands(run_dir, port)
    logger.info("testing run with run_dir %s - vm command: %s", run_dir, shlex.join(command))
    logger.info("testing run with run_dir %s - ssh command: %s", run_dir, shlex.join(ssh))
    logger.info(
        "on the guest vm, run: "
        "cd /opt/offline-test/project; "
        "GRADLE_USER_HOME=/opt/offline-test/gradle-offline ./gradlew --offline "
        "-DcreatingCache=false -Dmaven.repo.local=/opt/offline-test/m2 clean build bootJar shadowJar"
    )
