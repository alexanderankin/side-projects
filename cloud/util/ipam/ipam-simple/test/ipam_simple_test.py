from datetime import datetime, timezone
from json import dumps
from pathlib import Path
from typing import Any, NotRequired, TypedDict

import pytest
from pytest import CaptureFixture, MonkeyPatch

import ipam_simple
from ipam_simple import execute_args, parse_args

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
        },
    ),
    pytest.param(
        "ipam_simple range reserve my_range --dry-run",
        {
            "command": "range",
            "range_subcommand": "reserve",
            "range": "my_range",
            "dry_run": True,
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


test_scenarios_params = [
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
                "out": '{"10.0.0.1": {"created_at": "2025-07-19T14:14:00.00Z"}}',
                "err": "",
            },
            {
                "command": "range reserve r1",
                "out": '{"10.0.0.2": {"created_at": "2025-07-19T14:14:00.00Z"}}',
                "err": "",
            },
            {
                "command": "range reserve r1",
                "out": '{"10.0.0.3": {"created_at": "2025-07-19T14:14:00.00Z"}}',
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
                "out": '{"10.0.0.1": {"created_at": "2025-07-19T14:14:00.00Z"}}',
            },
            {
                "command": "range reserve r1",
                "out": '{"10.0.0.2": {"created_at": "2025-07-19T14:14:00.00Z"}}',
            },
            {
                "command": "range reserve r1",
                "out": '{"10.0.0.3": {"created_at": "2025-07-19T14:14:00.00Z"}}',
            },
            {"command": "range unreserve r1 10.0.0.1", "out": ""},
            {
                "command": "range info r1",
                "out": dumps(
                    {
                        "range": "10.0.0.0/16",
                        "state": {
                            "10.0.0.2": {"created_at": "2025-07-19T14:14:00.00Z"},
                            "10.0.0.3": {"created_at": "2025-07-19T14:14:00.00Z"},
                        },
                    }
                ),
            },
            {
                "command": "range reserve r1",
                "out": '{"10.0.0.1": {"created_at": "2025-07-19T14:14:00.00Z"}}',
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
            {"command": "range add d1 --range 10.0.0.0/16"},
            {"command": "range add d1 --range 10.0.0.0/16", "error": True},
        ],
        id="reserve an ip in a range that is full",
    ),
]


@pytest.mark.parametrize("sequence", test_scenarios_params)
def test_scenarios(
    sequence: list[SequenceStep],
    tmp_path: Path,
    monkeypatch: MonkeyPatch,
    capfd: CaptureFixture[str],
) -> None:
    monkeypatch.setattr(
        ipam_simple, "get_default_config_location", lambda: tmp_path / "config.json"
    )
    monkeypatch.setattr(
        ipam_simple, "get_default_fs_file_path", lambda: tmp_path / "data.json"
    )
    now = datetime(2025, 7, 19, 14, 14, 0, 0, tzinfo=timezone.utc)
    monkeypatch.setattr(ipam_simple, "_now", lambda: now)

    for step in sequence:
        argv = step["command"].split(" ")
        if step.get("error", False):
            with pytest.raises((BaseException, SystemExit)):
                execute_args(parse_args(argv))
        else:
            execute_args(parse_args(argv))

        out, err = capfd.readouterr()

        if "out" in step:
            assert step["out"] == out.strip(), f"out on step {step}"

        if "err" in step:
            assert step["err"] == err.strip(), f"err on step {step}"
        else:
            assert "" == err.strip(), f"err was not blank on step {step}"
