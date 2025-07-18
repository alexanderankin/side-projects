import pytest

from ipam_simple import parse_args

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
        "ipam_simple range stats my_range",
        {"command": "range", "range_subcommand": "stats", "range": "my_range"},
    ),
]


@pytest.mark.parametrize("arg_string,expected_result", test_cli_params)
def test_cli(arg_string, expected_result):
    args = arg_string.split(" ")[1:]
    parsed_args = parse_args(args)
    assert expected_result == parsed_args
