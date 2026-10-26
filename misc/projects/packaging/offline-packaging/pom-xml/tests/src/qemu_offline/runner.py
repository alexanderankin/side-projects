"""Build online, reboot without a NIC, and prove the exported Maven cache works."""

import argparse
import base64
import datetime
import io
import json
import logging
import platform
import shlex
import shutil
import subprocess
import sys
import tarfile
import tempfile
import threading
import time
import uuid
import xml.etree.ElementTree as ElT
from importlib.resources import files
from io import BufferedWriter
from pathlib import Path

from textwrap import dedent
from typing import AnyStr, IO

from qemu_offline.runs import SSH_KEY, list_runs, print_exploration, select_run, xml_text

logger = logging.getLogger(__name__)

POM_XML_FOLDER = Path(__file__).parent.parent.parent.parent
DEFAULT_IMAGE = (
    Path(__file__).parent
    / ".."
    / ".."
    / ".."
    / ".."
    / ".."
    / ".."
    / ".."
    / ".."
    / "cloud"
    / "vm"
    / "qemu-cloud-init"
    / "build"
    / "resolute-server-cloudimg-arm64.img"
)

pkg_files: Path = Path(str(files("qemu_offline")))


def arm64_firmware_path(qemu) -> Path | None:
    candidates = [
        Path(qemu).resolve().parent.parent / "share/qemu/edk2-aarch64-code.fd",
        Path("/opt/homebrew/share/qemu/edk2-aarch64-code.fd"),
        Path("/usr/share/qemu-efi-aarch64/QEMU_EFI.fd"),
        Path("/usr/share/AAVMF/AAVMF_CODE.fd"),
        Path("/usr/share/qemu/edk2-aarch64-code.fd"),
    ]
    return next((p for p in candidates if p.is_file()), None)


def seed_files(run_dir: Path, run_id: str, project: Path):
    archive = io.BytesIO()
    with tarfile.open(fileobj=archive, mode="w:gz") as tar:
        for name in ("build.gradle", "settings.gradle", "gradlew", "gradle", "src"):
            tar.add(project / name, arcname=name)

    payloads = {
        "/opt/offline-test/project.tar.gz": archive.getvalue(),
        "/opt/offline-test/run-id": run_id.encode(),
        "/opt/offline-test/guest.sh": pkg_files.joinpath("guest.sh").read_bytes(),
        "/opt/offline-test/verify.gradle": pkg_files.joinpath("verify.gradle").read_bytes(),
        "/etc/systemd/system/offline-packaging.service": dedent(
            """
            [Unit]
            Description=Offline packaging integration test
            After=local-fs.target
            [Service]
            Type=oneshot
            ExecStart=/bin/bash /opt/offline-test/guest.sh offline
            TimeoutStartSec=infinity
            [Install]
            WantedBy=multi-user.target
            """
        ).encode(),
    }
    config = {
        "package_update": False,
        "ssh_pwauth": False,
        "ssh_authorized_keys": [(run_dir / f"{SSH_KEY}.pub").read_text().strip()],
        "write_files": [
            {
                "path": path,
                "encoding": "b64",
                "content": base64.b64encode(content).decode(),
            }
            for path, content in payloads.items()
        ],
        "runcmd": [
            ["mkdir", "-p", "/opt/offline-test/project"],
            [
                "tar",
                "-xzf",
                "/opt/offline-test/project.tar.gz",
                "-C",
                "/opt/offline-test/project",
            ],
            ["bash", "/opt/offline-test/guest.sh", "online"],
        ],
    }

    # JSON is valid YAML; no host PyYAML dependency is needed.
    (run_dir / "user-data").write_text("#cloud-config\n" + json.dumps(config))
    (run_dir / "meta-data").write_text(json.dumps({"instance-id": run_id, "local-hostname": "offline-packaging"}))


def qemu_command(qemu: str, firmware: Path | None, run_dir, memory: int, cpus: int, phase: str) -> list[str]:
    def escape(path):
        return str(path).replace(",", ",,")

    command = [
        qemu,
        "-machine",
        "virt,accel=hvf:kvm",
        "-cpu",
        "host",
        "-m",
        str(memory),
        "-smp",
        str(cpus),
        *(
            [
                "-bios",
                str(firmware),
            ]
            if firmware
            else []
        ),
        "-drive",
        f"file={escape(run_dir / 'disk.qcow2')},format=qcow2,if=virtio",
        "-display",
        "none",
        "-monitor",
        "none",
        "-serial",
        "stdio",
        "-no-reboot",
    ]
    if phase == "online":
        command += [
            "-drive",
            f"file={escape(run_dir / 'seed.iso')},format=raw,media=cdrom",
            "-netdev",
            "user,id=net0",
            "-device",
            "virtio-net-pci,netdev=net0",
        ]
    elif phase == "offline":
        command += ["-nic", "none"]
    else:
        raise ValueError(f"Unknown phase: {phase}")
    return command


def run_phase(command: list[str], run_dir: Path, run_id: str, phase: str, timeout: int):
    (run_dir / f"phase-command-{phase}.json").write_text(json.dumps(command, indent=2) + "\n")
    logger.info("run_phase: %s: %s", phase, shlex.join(command))

    stdout_path, stderr_path = run_dir / f"phase-{phase}-stdout.log", run_dir / f"phase-{phase}-stderr.log"

    started = time.monotonic()

    with stdout_path.open("wb") as stdout_log, stderr_path.open("wb") as stderr_log:
        process = subprocess.Popen(
            command,
            stdin=subprocess.DEVNULL,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )

        def copy_stream(src: IO[AnyStr], log_prefix: str, writer: BufferedWriter):
            for line in src:
                logger.debug("%s: %s", log_prefix, line)
                writer.writelines([line])
                writer.flush()

        stdout_thread = threading.Thread(target=copy_stream, args=(process.stdout, "stdout", stdout_log))
        stderr_thread = threading.Thread(target=copy_stream, args=(process.stderr, "stderr", stderr_log))

        stdout_thread.start()
        stderr_thread.start()

        try:
            code = process.wait(timeout=timeout)
        finally:
            if process.poll() is None:
                process.terminate()
                try:
                    process.wait(timeout=timeout)
                except subprocess.TimeoutExpired:
                    process.kill()
                    process.wait()

            stdout_thread.join()
            stderr_thread.join()

    paths = [stdout_path, stderr_path]
    output = "\n".join([s.read_text(errors="replace") for s in paths])

    lines = output.replace("\r", "").splitlines()
    marker = f"QEMU_TEST:{run_id}:{phase}: "
    errs = {
        'err_bad_exit_code': code != 0,
        'err_pass_missing': marker + "PASS" not in lines,
        'err_fail_present': any(line.startswith(marker + "FAIL") for line in lines),
    }
    if any(errs.values()):
        raise RuntimeError(f"phase '{phase}' failed (QEMU exit {code}): {errs}; see {paths}")

    duration = datetime.timedelta(seconds=time.monotonic() - started)
    logger.info("phase %s passed in %s, logs: %s", phase, duration, paths)


def our_ubuntu_arch() -> str:
    return {
        "arm64": "arm64",
        "x86_64": "amd64",
    }[platform.machine()]


qemu_commands = {"amd64": "qemu-system-x86_64", "arm64": "qemu-system-aarch64"}


def our_qemu_system_binary() -> str:
    return qemu_commands[our_ubuntu_arch()]


def main() -> int:
    from enum import Enum

    class L(Enum):
        ERROR = logging.ERROR
        WARN = logging.WARN
        INFO = logging.INFO
        DEBUG = logging.DEBUG

    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--log-level", type=L, choices=list(L), default=L.INFO)

    parser.add_argument(
        "command",
        nargs="?",
        choices=("run", "list", "test"),
        help="Run the suite (default), list runs, or print an offline exploration command",
    )
    parser.add_argument("run_name", nargs="?", help="Run directory name for 'test'")
    parser.add_argument("--ssh-port", type=int, default=2222, help="Localhost SSH port for exploration (default: 2222)")
    parser.add_argument("--project", type=Path, default=POM_XML_FOLDER, help="pom-xml folder")
    parser.add_argument(
        "--image",
        type=Path,
        default=DEFAULT_IMAGE,
        help="Cloud image (default: shared cache relative to the project)",
    )
    parser.add_argument("--firmware", type=Path)
    parser.add_argument("--qemu", default=our_qemu_system_binary())
    parser.add_argument("--memory", type=int, default=4096, help="Guest RAM in MiB (default: 4096)")
    parser.add_argument("--cpus", type=int, default=2)
    parser.add_argument("--timeout", type=int, default=1800, help="Seconds allowed per boot")
    args = parser.parse_args()
    logging.basicConfig(level=args.log_level.value, format="%(asctime)s - %(levelname)s - %(message)s")

    project = args.project.resolve()
    if not (project / "build.gradle").is_file():
        parser.error(f"Gradle project not found: {project}; supply --project PATH")

    output_dir = project / "build/qemu-tests"
    if args.command == "list":
        list_runs(output_dir)
        return 0
    elif args.command == "test":
        if not args.run_name:
            parser.error("'test' requires a run directory name from 'list'")
        try:
            print_exploration(select_run(output_dir, args.run_name), args.ssh_port)
        except (OSError, ValueError) as exc:
            parser.error(str(exc))
        return 0
    elif args.command == "run":
        pass
    else:
        parser.error("need args.command")

    if not 1 <= args.ssh_port <= 65535:
        parser.error("SSH port must be between 1 and 65535")

    for executable in (args.qemu, "qemu-img", "xorriso", "ssh-keygen"):
        if not shutil.which(executable):
            parser.error(f"Missing required executable: {executable}")

    image: Path = args.image
    if not image.is_file():
        parser.error(f"Cached cloud image does not exist: {image}")

    firmware: Path | None = args.firmware or arm64_firmware_path(shutil.which(args.qemu))
    if our_ubuntu_arch() == "arm64":
        if firmware is None or not firmware.is_file():
            parser.error("ARM64 UEFI firmware not found; supply --firmware PATH")
        assert firmware is not None

    if min(args.memory, args.cpus, args.timeout) <= 0:
        parser.error("Memory, CPUs, and timeout must be positive")

    logger.info("parsed args: %s", args)

    output_dir.mkdir(parents=True, exist_ok=True)
    logger.info("output (build/qemu-tests) directory: %s", output_dir)

    run_timestamp = int(time.time())
    run_dir = Path(tempfile.mkdtemp(prefix=f"run-{run_timestamp}", dir=output_dir))
    logger.info("run dir: %s", run_dir.name)
    run_id = f"{run_timestamp}-{uuid.uuid4().hex}"
    logger.info("run id: %s", run_id)

    n = parser.prog
    suite = ElT.Element("testsuite", name=n, tests="2", failures="0", skipped="0")
    failure = None
    try:
        ssh_key_file = str(run_dir / SSH_KEY)
        logger.info("generating ssh key in %s", ssh_key_file)
        subprocess.run(["ssh-keygen", "-N", "", "-C", run_id, "-f", ssh_key_file], check=True)

        logger.info("creating vm image 'disk.qcow2' with 'qemu-img create -b'")
        info = json.loads(subprocess.check_output(["qemu-img", "info", "--output=json", str(image)]))
        subprocess.run(
            [
                "qemu-img",
                "create",
                "-f",
                "qcow2",
                "-F",
                info["format"],
                "-b",
                str(image),
                str(run_dir / "disk.qcow2"),
                "24G",
            ],
            check=True,
        )

        logger.info("creating seed files")
        seed_files(run_dir, run_id, project)
        logger.info("combining seed files with xorriso")
        subprocess.run(
            [
                "xorriso",
                "-as",
                "mkisofs",
                "-quiet",
                "-volid",
                "cidata",
                "-joliet",
                "-rock",
                "-output",
                str(run_dir / "seed.iso"),
                str(run_dir / "user-data"),
                str(run_dir / "meta-data"),
            ],
            check=True,
        )
        for phase in ("online", "offline"):
            logger.info("starting phase %s", phase)
            case = ElT.SubElement(suite, "testcase", name=phase, classname="qemu.offline_packaging")
            started = time.monotonic()
            try:
                firmware_resolved = firmware.resolve() if firmware else None
                phase_command = qemu_command(args.qemu, firmware_resolved, run_dir, args.memory, args.cpus, phase)
                logger.info("running phase %s with command %s", phase, phase_command)
                run_phase(phase_command, run_dir, run_id, phase, args.timeout)
            except (Exception, KeyboardInterrupt) as exc:
                ElT.SubElement(case, "failure", message=xml_text(str(exc))).text = xml_text(str(exc))
                raise
            finally:
                case.set("time", f"{time.monotonic() - started:.3f}")
    except (Exception, KeyboardInterrupt) as exc:  # noqa: BLE001
        # CLI boundary: record every failure in JUnit and return a nonzero exit.
        failure = exc
        if len(suite) == 0:
            case = ElT.SubElement(suite, "testcase", name="setup", classname="qemu.offline_packaging")
            ElT.SubElement(case, "failure", message=xml_text(str(exc)))
        if not any(case.get("name") == "offline" for case in suite):
            ElT.SubElement(
                ElT.SubElement(suite, "testcase", name="offline"),
                "skipped",
                message="Online/setup failed",
            )
    finally:
        suite.set("tests", str(len(suite)))
        suite.set("failures", str(len(suite.findall("./testcase/failure"))))
        suite.set("skipped", str(len(suite.findall("./testcase/skipped"))))
        ElT.ElementTree(suite).write(run_dir / "junit.xml", encoding="utf-8", xml_declaration=True)
    if failure is not None:
        print(f"FAIL: {failure}\nArtifacts: {run_dir}", file=sys.stderr)
        return 1
    print(f"PASS: online cache creation and offline rebuild. Report: {run_dir / 'junit.xml'}")
    (run_dir / "explore-ready").write_text("Offline test passed; guest test service disabled; SSH provisioned.\n")
    print_exploration(run_dir, args.ssh_port)
    return 0


if __name__ == "__main__":
    sys.exit(main())
