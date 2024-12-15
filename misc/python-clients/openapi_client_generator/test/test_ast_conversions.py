from logging import getLogger
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
from test.fixtures.example_inner import ExampleInner

logger = getLogger()


@pytest.mark.parametrize(
    "object_from_module",
    [
        pytest.param(Example, id="Example"),
        pytest.param(ExampleInner, id="ExampleInner"),
    ],
)
def test_ast_conversions_round_trip(object_from_module: Any):
    logger.debug("%s", object_from_module)
    source = object_to_module_source(object_from_module)
    d = ast_to_dict(source_to_ast(source))
    rt = ast_to_source(dict_to_ast(d))

    assert _rm_ws(source) == _rm_ws(rt)


def _rm_ws(s):
    "".join(s.split())
