import ast as ast_module
from ast import AST, parse, unparse
from inspect import getmodule, getsource
from logging import getLogger
from typing import Any, cast

ObjectFromModule = Any

logger = getLogger("openapi_client_generator")


def object_to_module_source(object_from_module: ObjectFromModule) -> str:
    try:
        module = getmodule(object_from_module)
        source_code = getsource(module)
        return source_code
    except Exception as e:
        logger.exception("object_to_module_source failed")
        raise RuntimeError("object_to_module_source failed") from e


def source_to_ast(source: str) -> AST:
    parsed_ast = parse(source)
    return parsed_ast


def ast_to_dict(ast_node: AST) -> dict[str, Any]:
    result = _ast_node_to_dict(ast_node)
    return result


def dict_to_ast(ast_dict: dict[str, Any]) -> AST:
    result = _dict_to_ast_node(ast_dict)
    return result


def ast_to_source(ast_node: AST) -> str:
    result = unparse(ast_node)
    return result


def _ast_node_to_dict(ast_node: AST | Any) -> dict[str, Any]:
    if not isinstance(ast_node, AST):
        return ast_node

    the_node_fields = {}
    the_node_as_dict = {"type": type(ast_node).__name__, "fields": the_node_fields}

    # noinspection PyProtectedMember
    for f in ast_node._fields:
        f_value: AST | list[AST] | None = getattr(ast_node, f, None)

        if f_value is None:
            the_node_fields[f] = None
        if isinstance(f_value, list):
            the_node_fields[f] = [_ast_node_to_dict(f) for f in f_value]
        else:
            the_node_fields[f] = _ast_node_to_dict(f_value)

    return the_node_as_dict


def _dict_to_ast_node(ast_dict: dict[str, Any] | Any) -> AST | None:
    if not isinstance(ast_dict, dict):
        return ast_dict

    node_type = ast_dict["type"]
    node_class = getattr(ast_module, node_type)
    node_class_ast = cast(type[AST], node_class)
    node = node_class_ast()

    for f, f_value in ast_dict["fields"].items():
        if f_value is None:
            setattr(node, f, None)
        if isinstance(f_value, list):
            setattr(node, f, [_dict_to_ast_node(v) for v in f_value])
        else:
            setattr(node, f, _dict_to_ast_node(f_value))

    return node
