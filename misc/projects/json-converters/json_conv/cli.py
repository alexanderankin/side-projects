from __future__ import annotations

from argparse import ArgumentParser, BooleanOptionalAction, Namespace
from json import dump, dumps, load
from pathlib import Path
from sys import argv, stderr, stdin, stdout
from tomllib import loads as toml_loads
from typing import Any, Iterable

from yaml import safe_dump as yaml_safe_dump, safe_load as yaml_safe_load


# ---------- Helpers (stream-friendly) ----------


def eprint(*args: Any) -> None:
    print(*args, file=stderr)


def json_dump_streaming_array(items: Iterable[Any]) -> None:
    """
    Stream JSON array to stdout without building it in memory.
    """
    out = stdout
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

def cmd_json2yaml(_ns: Namespace) -> int:
    data = load(stdin)
    yaml_safe_dump(data, stdout, sort_keys=False, allow_unicode=True)
    return 0


def cmd_yaml2json(_ns: Namespace) -> int:
    data = yaml_safe_load(stdin)
    dump(data, stdout, ensure_ascii=False)
    stdout.flush()
    return 0


def cmd_toml2json(_ns: Namespace) -> int:
    data = toml_loads(stdin.read())
    dump(data, stdout, ensure_ascii=False)
    stdout.flush()
    return 0


def cmd_json2toml(_ns: Namespace) -> int:
    raise Exception("not implemented")


def cmd_json2csv(ns: Namespace) -> int:
    headers: bool = ns.headers
    writer = writer(stdout, lineterminator="\n")
    data = load(stdin)

    if headers:
        if not isinstance(data, list) or (data and not isinstance(data[0], dict)):
            eprint("json2csv --headers expects a JSON array of objects")
            return 2
        if not data:
            return 0
        hdrs = list(data[0].keys())
        writer.writerow(hdrs)
        for obj in data:
            if not isinstance(obj, dict):
                eprint("json2csv --headers expects a JSON array of objects")
                return 2
            row = ["" if obj.get(k) is None else str(obj.get(k)) for k in hdrs]
            writer.writerow(row)
    else:
        if not isinstance(data, list):
            eprint("json2csv --no-headers expects a JSON array of arrays")
            return 2
        for arr in data:
            if not isinstance(arr, list):
                eprint("json2csv --no-headers expects a JSON array of arrays")
                return 2
            writer.writerow(["" if v is None else str(v) for v in arr])

    return 0


def cmd_csv2json(ns: Namespace) -> int:
    reader = reader(stdin)
    headers: bool = ns.headers

    if headers:
        try:
            hdrs = next(reader)
        except StopIteration:
            stdout.write("[]")
            return 0

        def gen():
            for row in reader:
                # pad or trim
                if len(row) < len(hdrs):
                    row = row + [""] * (len(hdrs) - len(row))
                elif len(row) > len(hdrs):
                    row = row[:len(hdrs)]
                yield {h: v for h, v in zip(hdrs, row)}

        json_dump_streaming_array(gen())
    else:
        json_dump_streaming_array(reader)

    return 0


# ---------- CLI dispatch ----------

def _build_parser() -> ArgumentParser:
    p = ArgumentParser(
        prog="json-conv",
        description="json/yaml/toml/csv converters via stdin/stdout",
    )
    sub = p.add_subparsers(dest="command", required=False)

    def csv_flags(sp_: ArgumentParser):
        g = sp_.add_mutually_exclusive_group()
        g.add_argument(
            "--headers",
            dest="headers",
            action=BooleanOptionalAction,
            default=True,
            help="Treat first row as headers (default true).",
        )
        return sp_

    for name, fn, add_flags in [
        ("json2yaml", cmd_json2yaml, None),
        ("yaml2json", cmd_yaml2json, None),
        ("toml2json", cmd_toml2json, None),
        ("json2toml", cmd_json2toml, None),
        ("json2csv", cmd_json2csv, csv_flags),
        ("csv2json", cmd_csv2json, csv_flags),
    ]:
        sp = sub.add_parser(name, help=f"{name} converter")
        if add_flags:
            add_flags(sp)
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
    print()
    return result


if __name__ == "__main__":
    raise SystemExit(main())
