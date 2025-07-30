#!/usr/bin/env python3
from functools import lru_cache
from abc import ABC, abstractmethod
from argparse import ArgumentParser, ArgumentTypeError
from collections.abc import Callable
from datetime import datetime
from ipaddress import IPv4Network
from itertools import islice
from json import dumps, loads
from logging import getLogger
from pathlib import Path
from subprocess import CalledProcessError, run
from tempfile import TemporaryDirectory
from typing import Any, NotRequired, TypedDict
from os import getenv

VERSION = "0.1.0"

log = getLogger(__name__)


def _now() -> datetime:
    return datetime.now()


def truncate_datetime_to_millis_like_json(d: datetime) -> str:
    return d.strftime("%Y-%m-%dT%H:%M:%S.%f")[:-4] + "Z"


def get_default_config_location():
    ipam_profile = getenv("IPAM_SIMPLE_PROFILE")
    suffix = f"-{ipam_profile}" if ipam_profile is not None else ""
    return Path.home() / ".config" / f"ipam-simple{suffix}.json"


def get_default_fs_file_path():
    return Path.home() / ".cache" / "ipam-simple.json"


def get_default_config():
    return {
        "default_backend": "fs",
        "file_path": str(get_default_fs_file_path()),
    }


def read_config() -> dict[str, Any]:
    try:
        config = loads(get_default_config_location().read_text())
    except FileNotFoundError:
        config = None

    if isinstance(config, dict):
        return config

    config = get_default_config()
    get_default_config_location().write_text(dumps(config))
    return config


def write_config(config: dict[str, Any]) -> None:
    get_default_config_location().write_text(dumps(config))


def config_list(_: dict[str, Any]) -> None:
    config = read_config()
    print(dumps(config))


def config_get(args: dict[str, Any]) -> None:
    config = read_config()
    print(dumps(config[args["key"]]))


def config_set(args: dict[str, Any]) -> None:
    config = read_config()
    config[args["key"]] = args["value"]
    write_config(config)
    print(dumps(config))


def execute_config(args: dict[str, Any]) -> None:
    config_commands = {
        "list": config_list,
        "get": config_get,
        "set": config_set,
    }
    func = config_commands.get(args["config_subcommand"])
    if func is None:
        raise ArgumentTypeError(f"Invalid config command: {args['config_subcommand']}")
    func(args)


class RangeEntity(TypedDict):
    range: str
    comment: NotRequired[str]
    state: dict[str, dict[str, Any]]


class RangeBackend(ABC):
    config: dict[str, Any]

    def __init__(self, config: dict[str, Any]):
        self.config = config

    @abstractmethod
    def read_ranges(self) -> dict[str, RangeEntity]: ...

    @abstractmethod
    def write_ranges(self, ranges: dict[str, RangeEntity]) -> None: ...


class FileRangeBackend(RangeBackend):
    def __init__(self, config: dict[str, Any]):
        super().__init__(config)

        if not self.config:
            raise ValueError("need config")
        if "file_path" not in self.config:
            raise ValueError("need file_path in config")

    def read_ranges(self) -> dict[str, RangeEntity]:
        try:
            return loads(Path(self.config["file_path"]).read_text())
        except FileNotFoundError:
            content = dict()
            self.write_ranges(content)
            return content

    def write_ranges(self, ranges: dict[str, RangeEntity]) -> None:
        Path(self.config["file_path"]).write_text(dumps(ranges))


class S3RangeBackend(RangeBackend):
    def __init__(self, config: dict[str, Any]):
        super().__init__(config)

        if not self.config:
            raise ValueError("need config")
        if "s3_uri" not in self.config:
            raise ValueError("need file_path in config")

    def read_ranges(self) -> dict[str, RangeEntity]:
        with TemporaryDirectory() as tmp_dir:
            temp_range_file = Path(tmp_dir) / "ranges.json"
            try:
                run(
                    [
                        "aws",
                        "s3",
                        "cp",
                        "--content-type=application/json",
                        self.config["s3_uri"],
                        str(temp_range_file),
                    ],
                    check=True,
                    encoding="utf-8",
                    capture_output=True,
                )
            except CalledProcessError as e:
                if "fatal error: An error occurred (404)" in e.stderr:
                    new_ranges = dict()
                    self.write_ranges(new_ranges)
                    return new_ranges
                else:
                    raise e
            return loads(temp_range_file.read_text())

    def write_ranges(self, ranges: dict[str, RangeEntity]) -> None:
        with TemporaryDirectory() as tmp_dir:
            temp_range_file = Path(tmp_dir) / "ranges.json"
            temp_range_file.write_text(dumps(ranges))
            run(
                [
                    "aws",
                    "s3",
                    "cp",
                    "--content-type=application/json",
                    str(temp_range_file),
                    self.config["s3_uri"],
                ],
                check=True,
            )


def get_range_backend(_: dict[str, Any]) -> RangeBackend:
    # todo consider args when selecting backend not just default backend
    backends = {
        "fs": FileRangeBackend,
        "s3": S3RangeBackend,
    }

    config = read_config()
    b_name = config["default_backend"]
    b_class = backends.get(b_name)
    if b_class is None:
        known = backends.keys()
        raise ValueError(f"default backend {b_name} not known. known: {known}")

    backend = b_class(config)
    return backend


def execute_range_add(args: dict[str, Any]) -> None:
    backend = get_range_backend(args)
    ranges = backend.read_ranges()
    if args["name"] in ranges:
        raise ValueError(f"range already exists: {args['name']}")
    ranges[args["name"]] = RangeEntity(
        range=args["range"],
        # comment=args["comment"] if "comment" in args else "",
        **({"comment": args["comment"]} if args.get("comment") else dict()),
        state=dict(),
    )
    backend.write_ranges(ranges)


def execute_range_list(args: dict[str, Any]) -> None:
    backend = get_range_backend(args)
    ranges = backend.read_ranges()
    range_names = list(ranges.keys())
    print(dumps(range_names))


def execute_range_remove(args: dict[str, Any]) -> None:
    backend = get_range_backend(args)
    ranges = backend.read_ranges()
    old = ranges.pop(args["name"], None)
    if old is None:
        raise ValueError(f"no such range: {args['name']}")
    backend.write_ranges(ranges)


def execute_range_reserve(args: dict[str, Any]) -> None:
    backend = get_range_backend(args)
    ranges = backend.read_ranges()
    the_range = ranges.get(args["range"])
    if not the_range:
        raise ValueError(f"range {args['range']} not found")

    ip_range = the_range["range"]
    new_ip: str | None = None
    network = IPv4Network(ip_range)
    iterator = iter(network)
    iterator_sliced = islice(iterator, 2, network.num_addresses - 1)

    for ip in iterator_sliced:
        ip_str = str(ip)
        if ip_str not in the_range["state"]:
            new_ip = ip_str
            break

    if new_ip is None:
        raise ValueError(f"ip address {ip_range} is full")

    json_now = truncate_datetime_to_millis_like_json(_now())
    meta = loads(args.get("meta") or "{}")
    base_value = {"created_at": json_now, "managed": True}

    # intentionally unsafe lol
    # meta.update(base_value)
    base_value.update(meta)
    meta = base_value

    new_entry = {new_ip: meta}
    the_range["state"].update(new_entry)

    backend.write_ranges(ranges)
    print(dumps(new_entry))


def execute_range_info(args: dict[str, Any]) -> None:
    backend = get_range_backend(args)
    ranges = backend.read_ranges()
    print(dumps(ranges[args["range"]]))


def execute_range_unreserve(args: dict[str, Any]) -> None:
    backend = get_range_backend(args)
    ranges = backend.read_ranges()
    the_range = ranges.get(args["range"])
    if not the_range:
        raise ValueError(f"range {args['range']} not found")

    reservation = args["reservation"]

    state = the_range["state"]
    if reservation not in state:
        raise ValueError(f"reservation {reservation} not in '{args['range']}'")

    old = state[reservation]
    managed = isinstance(old, dict) and old.get("managed")
    if not managed:
        raise ValueError(f"reservation {reservation} in '{args['range']}' is not managed by this script")
    del state[reservation]

    backend.write_ranges(ranges)


def execute_range(args: dict[str, Any]) -> None:
    range_commands = {
        "add": execute_range_add,
        "list": execute_range_list,
        "remove": execute_range_remove,
        "reserve": execute_range_reserve,
        "info": execute_range_info,
        "unreserve": execute_range_unreserve,
    }
    func = range_commands.get(args["range_subcommand"])
    if func is None:
        raise ArgumentTypeError(f"Invalid range command: {args['range_subcommand']}")
    func(args)


def execute_args(args: dict[str, Any]) -> None:
    commands = {
        "config": execute_config,
        "range": execute_range,
    }

    func: Callable[[dict[str, Any]], None] | None = commands.get(args["command"])
    if func is None:
        raise ArgumentTypeError(f"Invalid command: {args['command']}")
    func(args)


@lru_cache()
def get_parser() -> ArgumentParser:
    parser = ArgumentParser(prog="ipam_simple")
    parser.add_argument("-v", "--version", action="version", version=VERSION)

    subparsers = parser.add_subparsers(dest="command", required=True)

    # config command
    config_parser = subparsers.add_parser("config")
    config_subparsers = config_parser.add_subparsers(
        dest="config_subcommand", required=True
    )

    config_subparsers.add_parser("list")

    get_parser = config_subparsers.add_parser("get")
    get_parser.add_argument("key")

    set_parser = config_subparsers.add_parser("set")
    set_parser.add_argument("key")
    set_parser.add_argument("value")

    config_subparsers.add_parser("reset")

    # range command
    range_parser = subparsers.add_parser("range")
    range_subparsers = range_parser.add_subparsers(
        dest="range_subcommand", required=True
    )

    range_subparsers.add_parser("list")

    add_parser = range_subparsers.add_parser("add")
    add_parser.add_argument("name")
    add_parser.add_argument("--range", required=True)
    add_parser.add_argument("--comment", default=None)
    add_parser.add_argument("--dry-run", action="store_true")

    remove_parser = range_subparsers.add_parser("remove")
    remove_parser.add_argument("name")
    remove_parser.add_argument("--dry-run", action="store_true")

    reserve_parser = range_subparsers.add_parser("reserve")
    reserve_parser.add_argument("range")
    reserve_parser.add_argument("--meta", help="JSON string to add to the reservation object", default="{}")
    reserve_parser.add_argument("--dry-run", action="store_true")

    unreserve_parser = range_subparsers.add_parser("unreserve")
    unreserve_parser.add_argument("range")
    unreserve_parser.add_argument("reservation")
    unreserve_parser.add_argument("--dry-run", action="store_true")

    info_parser = range_subparsers.add_parser("info")
    info_parser.add_argument("range")
    return parser


def parse_args(args: list[str]) -> dict[str, Any]:
    parser = get_parser()
    args = parser.parse_args(args)
    return {**vars(args)}


def main():
    from sys import argv
    from logging import basicConfig, WARN, DEBUG

    debug = bool(getenv("DEBUG", None))

    basicConfig(level=DEBUG if debug else WARN)
    execute_args(parse_args(argv[1:]))


if __name__ == "__main__":
    main()
