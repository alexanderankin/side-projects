from json import dumps
from logging import getLogger
from re import split
from typing import Any

import pytest

from openapi_client_generator.ast_conversions import (
    ast_to_dict,
    ast_to_source,
    dict_to_ast,
    object_to_module_source,
    source_to_ast,
)
from test.fixtures.example import Example
from test.fixtures.example_docs import ExampleDocs
from test.fixtures.example_inner import ExampleInner

logger = getLogger()


@pytest.mark.parametrize(
    "object_from_module",
    [
        pytest.param(Example, id="Example"),
        pytest.param(ExampleInner, id="ExampleInner"),
        pytest.param(ExampleDocs, id="ExampleDocs"),
    ],
)
def test_ast_conversions_round_trip(object_from_module: Any):
    logger.debug("%s", object_from_module)
    source = object_to_module_source(object_from_module)
    d = ast_to_dict(source_to_ast(source))
    _ = dumps(d, indent=4)
    rt = ast_to_source(dict_to_ast(d))

    assert _rm_whitespace(source) == _rm_whitespace(rt)


def _rm_whitespace(s):
    "\n".join(split("\n+", s))
