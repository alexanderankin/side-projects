from functools import lru_cache
from json import loads
from os import listdir
from pathlib import Path
from typing import Any

from yaml import parse

EXAMPLE_SPECS = Path(__file__).parent / "example-specs"

PathData = tuple[Path, dict[str, Any]]


def _openapi(f: str) -> PathData | None:
    full_path = EXAMPLE_SPECS / f
    contents = full_path.read_text()
    data = loads(contents) if f.endswith(".json") else parse(contents)
    if "openapi" not in data:
        return None
    return full_path, data


@lru_cache
def openapi_examples() -> list[PathData]:
    files = listdir(EXAMPLE_SPECS)
    result = [_openapi(f) for f in files]
    result_not_none = [r for r in result if r is not None]
    return result_not_none


@lru_cache
def docker_converted_openapi():
    return _openapi("docs.docker.com_reference_api_engine_version_v1.45.converted.json")


if __name__ == '__main__':
    examples = docker_converted_openapi()
    print(examples)
