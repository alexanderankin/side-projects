from __future__ import annotations

from os import linesep
from csv import reader, writer
from argparse import ArgumentParser, BooleanOptionalAction, Namespace
from importlib.metadata import version
from json import dump, dumps, load
from pathlib import Path
from sys import argv, stderr, stdin, stdout
from tomllib import loads as toml_loads
from typing import Any, Iterable, TextIO

from yaml import safe_dump as yaml_safe_dump, safe_load as yaml_safe_load

__import_lib_version__ = version("json-conv")
__version__ = f"json-conv {__import_lib_version__}"


# ---------- Helpers (stream-friendly) ----------


def eprint(*args: Any) -> None:
    print(*args, file=stderr)


def json_dump_streaming_array(items: Iterable[Any], out: TextIO) -> None:
    """
    Stream JSON array to stdout without building it in memory.
    """
    out.write("[")
    first = True
    for item in items:
        if first:
            first = False
        else:
            out.write(",")
        out.write(dumps(item, ensure_ascii=False))
    out.write("]")
    out.flush()


# ---------- Converters ----------
def _parse_io(ns: Namespace) -> tuple[TextIO, TextIO]:
    input = getattr(ns, "input", None)
    if input:
        open_input = open(input)
    else:
        open_input = stdin
    output = getattr(ns, "output", None)
    if output:
        open_output = open(output)
    else:
        open_output = stdout
    return open_input, open_output


def cmd_json2yaml(_ns: Namespace) -> int:
    open_input, open_output = _parse_io(_ns)
    data = load(open_input)
    yaml_safe_dump(data, open_output, sort_keys=False, allow_unicode=True)
    open_output.flush()
    return 0


def cmd_yaml2json(_ns: Namespace) -> int:
    open_input, open_output = _parse_io(_ns)
    data = yaml_safe_load(open_input)
    dump(data, open_output, ensure_ascii=False)
    open_output.write(linesep)
    open_output.flush()
    return 0


def cmd_toml2json(_ns: Namespace) -> int:
    open_input, open_output = _parse_io(_ns)
    data = toml_loads(open_input.read())
    dump(data, open_output, ensure_ascii=False)
    open_output.write(linesep)
    open_output.flush()
    return 0


def cmd_json2toml(_ns: Namespace) -> int:
    raise Exception("not implemented")


def cmd_json2csv(ns: Namespace) -> int:
    headers: bool = ns.headers
    open_input, open_output = _parse_io(ns)
    csv_writer = writer(open_output, lineterminator="\n")
    data = load(open_input)

    if headers:
        if not isinstance(data, list) or (data and not isinstance(data[0], dict)):
            eprint("json2csv --headers expects a JSON array of objects")
            return 2
        if not data:
            return 0
        hdrs = list(data[0].keys())
        csv_writer.writerow(hdrs)
        for obj in data:
            if not isinstance(obj, dict):
                eprint("json2csv --headers expects a JSON array of objects")
                return 2
            row = ["" if obj.get(k) is None else str(obj.get(k)) for k in hdrs]
            csv_writer.writerow(row)
    else:
        if not isinstance(data, list):
            eprint("json2csv --no-headers expects a JSON array of arrays")
            return 2
        for arr in data:
            if not isinstance(arr, list):
                eprint("json2csv --no-headers expects a JSON array of arrays")
                return 2
            csv_writer.writerow(["" if v is None else str(v) for v in arr])

    return 0


def cmd_csv2json(ns: Namespace) -> int:
    open_input, open_output = _parse_io(ns)
    csv_reader = reader(open_input)
    headers: bool = ns.headers

    if headers:
        try:
            hdrs = next(csv_reader)
        except StopIteration:
            open_output.write("[]")
            return 0

        def gen():
            for row in csv_reader:
                # pad or trim
                if len(row) < len(hdrs):
                    row = row + [""] * (len(hdrs) - len(row))
                elif len(row) > len(hdrs):
                    row = row[: len(hdrs)]
                yield {h: v for h, v in zip(hdrs, row)}

        json_dump_streaming_array(gen(), open_output)
    else:
        json_dump_streaming_array(csv_reader, open_output)

    open_output.write(linesep)
    return 0


# ---------- CLI dispatch ----------
def _add_csv_flags(sp_: ArgumentParser):
    g = sp_.add_mutually_exclusive_group()
    g.add_argument(
        "--headers",
        dest="headers",
        action=BooleanOptionalAction,
        default=True,
        help="Treat first row as headers (default true).",
    )
    return sp_


def _add_io_flags(sp_: ArgumentParser):
    sp_.add_argument("-i", "--input", dest="input", metavar="INPUT_FILE")
    sp_.add_argument("-o", "--output", dest="output", metavar="OUTPUT_FILE")


def _add_ver(sp_: ArgumentParser):
    sp_.add_argument("-v", "--version", action="version", version=__version__)


def _build_parser() -> ArgumentParser:
    p = ArgumentParser(
        prog="json-conv",
        description="json/yaml/toml/csv converters via stdin/stdout",
    )
    p.add_argument("-v", "--version", action="version", version=__import_lib_version__)
    sub = p.add_subparsers(dest="command", required=False)

    for name, fn, flag_fns in [
        ("json2yaml", cmd_json2yaml, [_add_ver, _add_io_flags]),
        ("yaml2json", cmd_yaml2json, [_add_ver, _add_io_flags]),
        ("toml2json", cmd_toml2json, [_add_ver, _add_io_flags]),
        ("json2toml", cmd_json2toml, [_add_ver, _add_io_flags]),
        ("json2csv", cmd_json2csv, [_add_ver, _add_io_flags, _add_csv_flags]),
        ("csv2json", cmd_csv2json, [_add_ver, _add_io_flags, _add_csv_flags]),
    ]:
        sp = sub.add_parser(name, help=f"{name} converter")
        for each_fn in flag_fns:
            each_fn(sp)
        sp.set_defaults(_handler=fn)

    return p


ALIASES = {"json2yaml", "yaml2json", "toml2json", "json2toml", "json2csv", "csv2json"}


def main(main_argv=None) -> int:
    args = list(argv if main_argv is None else main_argv)

    invoked = Path(args[0]).name.split(".")[0]
    if invoked in ALIASES and (len(args) == 1 or args[1] != invoked):
        args = [args[0], invoked] + args[1:]

    parser = _build_parser()
    ns = parser.parse_args(args[1:])

    if not getattr(ns, "command", None):
        parser.print_help(stderr)
        return 2

    handler = getattr(ns, "_handler", None)
    if not handler:
        parser.error("unknown command")

    result = int(handler(ns))
    return result


if __name__ == "__main__":
    raise SystemExit(main())
