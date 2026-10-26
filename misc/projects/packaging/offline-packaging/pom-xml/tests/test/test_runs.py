"""Saved runs must remain immutable and exploration must not expose an uplink."""

import json
import shlex

import pytest

from qemu_offline import runner, runs


@pytest.fixture
def completed_run(tmp_path):
    run_dir = tmp_path / "run-example"
    run_dir.mkdir()
    (run_dir / "junit.xml").write_text(
        '<testsuite failures="0"><testcase name="online"/><testcase name="offline"/></testsuite>'
    )
    (run_dir / "disk.qcow2").write_bytes(b"original disk")
    (run_dir / runs.SSH_KEY).write_text("test key")
    (run_dir / "explore-ready").touch()
    command = runner.qemu_command("qemu-system-aarch64", None, run_dir, 4096, 2, "offline")
    (run_dir / "phase-command-offline.json").write_text(json.dumps(command))
    return run_dir


def test_exploration_is_snapshot_with_only_restricted_localhost_ssh(completed_run):
    before = {p.name: p.read_bytes() for p in completed_run.iterdir()}
    command, ssh = runs.exploration_commands(completed_run, 2223)
    assert "-snapshot" in command
    assert command[command.index("-machine") + 1] == "virt,accel=hvf:kvm"
    assert command[command.index("-netdev") + 1] == (
        "user,id=inspect,restrict=on,ipv6=off,hostfwd=tcp:127.0.0.1:2223-:22"
    )
    assert "seed.iso" not in " ".join(command)
    assert ssh[ssh.index("-i") + 1] == str(completed_run / runs.SSH_KEY)
    assert ssh[ssh.index("-p") + 1] == "2223"
    assert {p.name: p.read_bytes() for p in completed_run.iterdir()} == before


def test_list_includes_legacy_and_incomplete_runs(completed_run, capsys):
    legacy = completed_run.parent / "run-legacy"
    legacy.mkdir()
    (legacy / "junit.xml").write_bytes((completed_run / "junit.xml").read_bytes())
    (completed_run.parent / "run-incomplete").mkdir()
    runs.list_runs(completed_run.parent)
    rows = {row.split()[0]: row.split()[1:] for row in capsys.readouterr().out.splitlines()[1:]}
    assert rows == {
        "run-example": ["PASSED", "ready"],
        "run-legacy": ["PASSED", "unavailable"],
        "run-incomplete": ["INCOMPLETE", "unavailable"],
    }
    with pytest.raises(ValueError, match="fresh test run"):
        runs.exploration_commands(legacy)


@pytest.mark.parametrize(
    ("report", "status"),
    [
        ('<testsuite><testcase name="offline"><failure/></testcase></testsuite>', "FAILED"),
        (
            '<testsuite><testcase name="offline"><failure>\x1b[31merror\x1b[0m</failure></testcase></testsuite>',
            "FAILED",
        ),
        (
            '<testsuite><testcase name="online"/><testcase name="offline"><skipped/></testcase></testsuite>',
            "INCOMPLETE",
        ),
        ("<testsuite/>", "INCOMPLETE"),
        ("broken xml", "INVALID"),
    ],
)
def test_only_successful_runs_can_be_explored(completed_run, report, status):
    (completed_run / "junit.xml").write_text(report)
    assert runs.run_status(completed_run) == status
    with pytest.raises(ValueError, match="did not pass"):
        runs.exploration_commands(completed_run)


@pytest.mark.parametrize("port", [0, -1, 65536])
def test_invalid_ssh_ports_are_rejected(completed_run, port):
    with pytest.raises(ValueError, match="SSH port"):
        runs.exploration_commands(completed_run, port)


def test_unexpected_saved_networking_is_rejected(completed_run):
    saved = completed_run / "phase-command-offline.json"
    saved.write_text(json.dumps(json.loads(saved.read_text()) + ["-netdev", "user,id=uplink"]))
    with pytest.raises(ValueError, match="unexpected networking"):
        runs.exploration_commands(completed_run)


@pytest.mark.parametrize("name", ["../run-example", "/run-example", "missing", "run-missing"])
def test_run_selection_rejects_paths_and_missing_runs(completed_run, name):
    with pytest.raises(ValueError):
        runs.select_run(completed_run.parent, name)


def test_test_command_only_prints_shell_quoted_commands(completed_run, monkeypatch, capsys):
    project = completed_run.parent
    (project / "build.gradle").touch()
    output = project / "build/qemu-tests"
    output.mkdir(parents=True)
    run_dir = completed_run.rename(output / completed_run.name)
    monkeypatch.setattr("sys.argv", ["qemu-offline-test", "test", run_dir.name, "--project", str(project)])

    def unexpected_process(*args, **kwargs):
        pytest.fail("Printing a command must not start a process")

    monkeypatch.setattr(runner.subprocess, "run", unexpected_process)
    monkeypatch.setattr(runner.subprocess, "Popen", unexpected_process)
    assert runner.main() == 0
    commands = [line for line in capsys.readouterr().out.splitlines() if not line.startswith("#")]
    assert len(commands) == 2
    assert shlex.split(commands[0]) == runs.exploration_commands(run_dir)[0]
