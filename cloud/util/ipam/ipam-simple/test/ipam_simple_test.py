from contextlib import AbstractContextManager
from dataclasses import dataclass, field
from datetime import datetime, timezone
from json import dumps, loads
from os import environ
from pathlib import Path
from re import compile, Pattern
from typing import Any, NotRequired, TypedDict

import pytest
from pytest import CaptureFixture, MonkeyPatch

import ipam_simple
from ipam_simple import (
    RangeBackend,
    RangeEntity,
    execute_args,
    get_default_config,
    get_default_config_location,
    parse_args,
)


@pytest.mark.parametrize("profile_value", [None, "a", "profile", "A"])
def test_get_default_config_location(profile_value):
    with TempEnvVar.of("IPAM_SIMPLE_PROFILE", profile_value):
        if profile_value:
            assert (
                f"ipam-simple-{profile_value}.json"
                == Path(get_default_config_location()).name
            )
        else:
            assert "ipam-simple.json" == Path(get_default_config_location()).name


test_cli_params = [
    pytest.param(
        "ipam_simple config list", {"command": "config", "config_subcommand": "list"}
    ),
    pytest.param(
        "ipam_simple config get default_backend",
        {"command": "config", "config_subcommand": "get", "key": "default_backend"},
    ),
    pytest.param(
        "ipam_simple config set default_backend fs",
        {
            "command": "config",
            "config_subcommand": "set",
            "key": "default_backend",
            "value": "fs",
        },
    ),
    pytest.param(
        "ipam_simple config reset", {"command": "config", "config_subcommand": "reset"}
    ),
    pytest.param(
        "ipam_simple range list", {"command": "range", "range_subcommand": "list"}
    ),
    pytest.param(
        "ipam_simple range add name --range 10.0.0.0/16 --comment hi",
        {
            "command": "range",
            "range_subcommand": "add",
            "name": "name",
            "range": "10.0.0.0/16",
            "comment": "hi",
            "dry_run": False,
        },
    ),
    pytest.param(
        "ipam_simple range add name --range 10.0.0.0/16 --comment hi --dry-run",
        {
            "command": "range",
            "range_subcommand": "add",
            "name": "name",
            "range": "10.0.0.0/16",
            "comment": "hi",
            "dry_run": True,
        },
    ),
    pytest.param(
        "ipam_simple range remove name",
        {
            "command": "range",
            "range_subcommand": "remove",
            "name": "name",
            "dry_run": False,
        },
    ),
    pytest.param(
        "ipam_simple range remove name --dry-run",
        {
            "command": "range",
            "range_subcommand": "remove",
            "name": "name",
            "dry_run": True,
        },
    ),
    pytest.param(
        "ipam_simple range reserve my_range",
        {
            "command": "range",
            "range_subcommand": "reserve",
            "range": "my_range",
            "dry_run": False,
            "meta": "{}",
        },
    ),
    pytest.param(
        "ipam_simple range reserve my_range --dry-run",
        {
            "command": "range",
            "range_subcommand": "reserve",
            "range": "my_range",
            "dry_run": True,
            "meta": "{}",
        },
    ),
    pytest.param(
        "ipam_simple range unreserve my_range 10.0.0.1",
        {
            "command": "range",
            "range_subcommand": "unreserve",
            "range": "my_range",
            "reservation": "10.0.0.1",
            "dry_run": False,
        },
    ),
    pytest.param(
        "ipam_simple range unreserve my_range 10.0.0.1 --dry-run",
        {
            "command": "range",
            "range_subcommand": "unreserve",
            "range": "my_range",
            "reservation": "10.0.0.1",
            "dry_run": True,
        },
    ),
    pytest.param(
        "ipam_simple range info my_range",
        {"command": "range", "range_subcommand": "info", "range": "my_range"},
    ),
]


@pytest.mark.parametrize("arg_string,expected_result", test_cli_params)
def test_cli(arg_string: str, expected_result: dict[str, Any]) -> None:
    args = arg_string.split(" ")[1:]
    parsed_args = parse_args(args)
    assert expected_result == parsed_args


class SequenceStep(TypedDict):
    command: str
    error: NotRequired[bool]
    out: NotRequired[str]
    err: NotRequired[str]


class InMemoryBackend(RangeBackend):
    ranges: dict[str, RangeEntity]

    def __init__(self, config: dict[str, Any]):
        super().__init__(config)
        self.ranges = dict()

    def read_ranges(self) -> dict[str, RangeEntity]:
        return self.ranges

    def write_ranges(self, ranges: dict[str, RangeEntity]) -> None:
        self.ranges = loads(dumps(ranges))


@dataclass
class ConfigBackend:
    data: dict[str, Any]

    def get_data(self) -> dict[str, Any]:
        return self.data

    def set_data(self, data: dict[str, Any]) -> None:
        self.data = data


test_scenarios_params = [
    pytest.param(
        [
            {
                "command": "config",
                "error": True,
                "err": compile("usage: .*"),
            },
        ],
        id="config",
    ),
    pytest.param(
        [
            {
                # read the default config and write it back to disk
                "command": "config list",
                "out": compile(
                    '{"default_backend":\\s*"fs",\\s*"file_path":\\s*".*\\.json"\\s*}'
                ),
            },
            {
                # read the config from disk
                "command": "config list",
                "out": compile(
                    '{"default_backend":\\s*"fs",\\s*"file_path":\\s*".*\\.json"\\s*}'
                ),
            },
        ],
        id="config list",
    ),
    pytest.param(
        [
            {"command": "config set file_path dne"},
            {"command": "config set default_backend s3"},
            {"command": "config get file_path", "out": '"dne"'},
            {
                "command": "config list",
                "out": '{"default_backend": "s3", "file_path": "dne"}',
            },
        ],
        id="config editing",
    ),
    pytest.param(
        [
            {"command": "range list", "error": False, "out": "[]", "err": ""},
            {"command": "range add r1 --range 10.0.0.0/16", "out": "", "err": ""},
            {"command": "range list", "out": '["r1"]', "err": ""},
        ],
        id="add a range",
    ),
    pytest.param(
        [
            {"command": "range list", "error": False, "out": "[]", "err": ""},
            {"command": "range add r1 --range 10.0.0.0/16", "out": "", "err": ""},
            {"command": "range add r2 --range 10.0.0.0/16", "out": "", "err": ""},
            {"command": "range list", "out": '["r1", "r2"]', "err": ""},
            {"command": "range remove r2", "out": "", "err": ""},
            {"command": "range list", "out": '["r1"]', "err": ""},
        ],
        id="add and remove a range",
    ),
    pytest.param(
        [
            {"command": "range list", "out": "[]", "err": ""},
            {"command": "range add r1 --range 10.0.0.0/16", "out": "", "err": ""},
            {
                "command": "range reserve r1",
                "out": '{"10.0.0.2": {"created_at": "2025-07-19T14:14:00.00Z", "managed": true}}',
                "err": "",
            },
            {
                "command": "range reserve r1",
                "out": '{"10.0.0.3": {"created_at": "2025-07-19T14:14:00.00Z", "managed": true}}',
                "err": "",
            },
            {
                "command": "range reserve r1",
                "out": '{"10.0.0.4": {"created_at": "2025-07-19T14:14:00.00Z", "managed": true}}',
                "err": "",
            },
        ],
        id="reserve an address",
    ),
    pytest.param(
        [{"command": "range reserve r1", "error": True}],
        id="reserve an address in a range that doesn't exist",
    ),
    pytest.param(
        [{"command": "range unreserve r1 10.0.0.1", "error": True}],
        id="unreserve an address in a range that doesn't exist",
    ),
    pytest.param(
        [
            {"command": "range add r1 --range 10.0.0.1"},
            {"command": "range unreserve r1 10.0.0.1", "error": True},
        ],
        id="unreserve an address that doesn't exist in a range",
    ),
    pytest.param(
        [
            {"command": "range list", "error": False, "out": "[]"},
            {"command": "range add r1 --range 10.0.0.0/16", "out": ""},
            {
                "command": "range reserve r1",
                "out": '{"10.0.0.2": {"created_at": "2025-07-19T14:14:00.00Z", "managed": true}}',
            },
            {
                "command": "range reserve r1",
                "out": '{"10.0.0.3": {"created_at": "2025-07-19T14:14:00.00Z", "managed": true}}',
            },
            {
                "command": "range reserve r1",
                "out": '{"10.0.0.4": {"created_at": "2025-07-19T14:14:00.00Z", "managed": true}}',
            },
            {"command": "range unreserve r1 10.0.0.2", "out": ""},
            {
                "command": "range info r1",
                "out": dumps(
                    {
                        "range": "10.0.0.0/16",
                        "state": {
                            "10.0.0.3": {
                                "created_at": "2025-07-19T14:14:00.00Z",
                                "managed": True,
                            },
                            "10.0.0.4": {
                                "created_at": "2025-07-19T14:14:00.00Z",
                                "managed": True,
                            },
                        },
                    }
                ),
            },
            {
                "command": "range reserve r1",
                "out": '{"10.0.0.2": {"created_at": "2025-07-19T14:14:00.00Z", "managed": true}}',
            },
        ],
        id="reserve an address out of order",
    ),
    pytest.param(
        [{"command": "range remove dne", "error": True}],
        id="remove a range that does not exist",
    ),
    pytest.param(
        [
            {"command": "range add d1 --range 10.0.0.0/16"},
            {"command": "range add d1 --range 10.0.0.0/16", "error": True},
        ],
        id="add a range that has a duplicate name",
    ),
    pytest.param(
        [
            {"command": "range add d1 --range 10.0.0.0/31"},
            {"command": "range reserve d1", "error": True},
        ],
        id="reserve an ip in a 31 range that is full",
    ),
    pytest.param(
        [
            {"command": "range add d1 --range 10.0.0.0/30"},
            {
                "command": "range reserve d1",
                "out": '{"10.0.0.2": {"created_at": "2025-07-19T14:14:00.00Z", "managed": true}}',
            },
            {"command": "range reserve d1", "error": True},
        ],
        id="reserve an ip in a 30 range that is full",
    ),
    pytest.param(
        [
            {"command": "range add d1 --range 10.0.0.0/29"},
            {
                "command": "range reserve d1",
                "out": '{"10.0.0.2": {"created_at": "2025-07-19T14:14:00.00Z", "managed": true}}',
            },
            {"command": "range reserve d1"},  # 3
            {"command": "range reserve d1"},  # 4
            {"command": "range reserve d1"},  # 5
            {
                "command": "range reserve d1",
                "out": '{"10.0.0.6": {"created_at": "2025-07-19T14:14:00.00Z", "managed": true}}',
            },
            {"command": "range reserve d1", "error": True},
            {"command": "range unreserve d1 10.0.0.2"},
            {
                "command": "range reserve d1",
                "out": '{"10.0.0.2": {"created_at": "2025-07-19T14:14:00.00Z", "managed": true}}',
            },
            {"command": "range reserve d1", "error": True},
            {"command": "range unreserve d1 10.0.0.3"},
            {
                "command": "range reserve d1",
                "out": '{"10.0.0.3": {"created_at": "2025-07-19T14:14:00.00Z", "managed": true}}',
            },
            {"command": "range reserve d1", "error": True},
        ],
        id="reserve an ip in a 29 range that is full",
    ),
    pytest.param(
        [
            {"command": "range add d1 --range 10.0.0.0/16"},
            {"command": 'range reserve d1 --meta {"managed":false}'},
            {"command": "range reserve d1"},
            {
                "command": "range info d1",
                "out": dumps(
                    {
                        "range": "10.0.0.0/16",
                        "state": {
                            "10.0.0.2": {
                                "created_at": "2025-07-19T14:14:00.00Z",
                                "managed": False,
                            },
                            "10.0.0.3": {
                                "created_at": "2025-07-19T14:14:00.00Z",
                                "managed": True,
                            },
                        },
                    }
                ),
            },
            {"command": "range unreserve d1 10.0.0.2", "error": True},
            {"command": "range unreserve d1 10.0.0.3"},
            {
                "command": "range info d1",
                "out": dumps(
                    {
                        "range": "10.0.0.0/16",
                        "state": {
                            "10.0.0.2": {
                                "created_at": "2025-07-19T14:14:00.00Z",
                                "managed": False,
                            },
                        },
                    }
                ),
            },
        ],
        id="unreserve a range that is managed externally fails",
    ),
]


@pytest.mark.parametrize("sequence", test_scenarios_params)
def test_scenarios(
    sequence: list[SequenceStep],
    tmp_path: Path,
    monkeypatch: MonkeyPatch,
    capfd: CaptureFixture[str],
) -> None:
    now = datetime(2025, 7, 19, 14, 14, 0, 0, tzinfo=timezone.utc)
    monkeypatch.setattr(ipam_simple, "_now", lambda: now)

    backend = InMemoryBackend(dict())
    monkeypatch.setattr(ipam_simple, "get_range_backend", lambda *a, **k: backend)

    config = ConfigBackend(get_default_config())
    monkeypatch.setattr(ipam_simple, "write_config", config.set_data)
    monkeypatch.setattr(ipam_simple, "read_config", config.get_data)

    for step in sequence:
        argv = step["command"].split(" ")
        if step.get("error", False):
            with pytest.raises((BaseException, SystemExit)):
                execute_args(parse_args(argv))
        else:
            execute_args(parse_args(argv))

        out, err = capfd.readouterr()

        if "out" in step:
            step_out = step["out"]
            if isinstance(step_out, str):
                assert step_out == out.strip(), f"out on step {step}"
            elif isinstance(step_out, Pattern):
                assert step_out.match(out.strip()) is not None, (
                    f"out on step {step} does not match regex"
                )

        if "err" in step:
            step_err = step["err"]
            if isinstance(step_err, str):
                assert step_err == err.strip(), f"err on step {step}"
            elif isinstance(step_err, Pattern):
                assert step_err.match(err) is not None, (
                    f"err on step {step} does not match regex"
                )
        else:
            assert "" == err.strip(), f"err was not blank on step {step}"


def test_backend_constructors():
    from ipam_simple import FileRangeBackend, S3RangeBackend

    with pytest.raises(ValueError):
        FileRangeBackend(dict())
    with pytest.raises(ValueError):
        S3RangeBackend(dict())

    with pytest.raises(ValueError):
        FileRangeBackend(dict(truthy="dict"))
    with pytest.raises(ValueError):
        S3RangeBackend(dict(truthy="dict"))


@dataclass
class TempEnvVar(AbstractContextManager):
    name: str
    value: str | None
    environ: dict[str, str] = field(default_factory=lambda: environ)
    old_value: str | None = None

    @staticmethod
    def of(name: str, value: str | None) -> "TempEnvVar":
        return TempEnvVar(name, value)

    def __enter__(self) -> "TempEnvVar":
        self.old_value = environ.pop(self.name, None)
        if self.value is not None:
            environ[self.name] = self.value
        return self

    def __exit__(self, *args) -> None:
        if self.old_value is None:
            environ.pop(self.name, None)
        else:
            environ[self.name] = self.old_value
        self.old_value = None


def test_temp_env_var():
    assert None == environ.get("test_temp_env_var")
    with TempEnvVar("test_temp_env_var", "value"):
        assert "value" == environ.get("test_temp_env_var")
    assert None == environ.get("test_temp_env_var")

    assert None is not environ.get("HOME")
    with TempEnvVar("HOME", "value"):
        assert "value" == environ.get("HOME")
    assert None is not environ.get("HOME")


def test_fs_backend(tmp_path: Path) -> None:
    from ipam_simple import FileRangeBackend

    backend = FileRangeBackend(dict(file_path=str(tmp_path / "data.json")))
    assert dict() == backend.read_ranges()
    ranges = dict()
    backend.write_ranges(ranges)
    assert ranges == backend.read_ranges()


'''
def test_s3_backend(tmp_path: Path) -> None:
    """
    this one works by first creating a localstack container,
    then configuring aws cli with a random profile,
    """
    from ipam_simple import S3RangeBackend

    with (
        TempEnvVar("AWS_CONFIG_FILE", str(tmp_path / "config")),
        TempEnvVar("AWS_SHARED_CREDENTIALS_FILE", str(tmp_path / "credentials")),
    ):
        backend = S3RangeBackend(dict(s3_uri="dict"))
        assert dict() == backend.read_ranges()
        ranges = dict()
        backend.write_ranges(ranges)
        assert ranges == backend.read_ranges()
        print("ok")
'''
