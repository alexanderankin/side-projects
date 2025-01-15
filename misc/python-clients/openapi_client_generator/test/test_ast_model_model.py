from textwrap import dedent

from openapi_client_generator.ast_conversions import ast_to_source, dict_to_ast
from openapi_client_generator.ast_model_model import (
    AstModel,
    AstModelField,
    AstModule,
    AstTypeAlias,
)


def render(ast_module: AstModule) -> str:
    ast_module_dict = ast_module.as_ast_dict()
    ast_module_ast = dict_to_ast(ast_module_dict)
    ast_module_source = ast_to_source(ast_module_ast)
    return ast_module_source


def test_render_plain_imports():
    ast_module = AstModule()
    ast_module.plain_imports = ["os", "time", "datetime"]
    ast_module_source = render(ast_module)
    # print(ast_module_source)
    assert (
        dedent("""
        import os
        import time
        import datetime
        """).strip()
        == ast_module_source
    )


def test_render_from_import_imports():
    ast_module = AstModule()
    ast_module.from_import_imports = [
        ("sys", ["argv", "version"]),
        ("datetime", ["datetime", "date"]),
    ]
    ast_module_source = render(ast_module)
    # print(ast_module_source)
    assert (
        dedent("""
        from sys import argv, version
        from datetime import datetime, date
        """).strip()
        == ast_module_source
    )


def test_imports_and_model():
    ast_module = AstModule()
    ast_module.from_import_imports = [
        ("datetime", ["datetime"]),
        ("uuid", ["UUID"]),
    ]
    ast_module.plain_imports = ["decimal"]

    ast_module.ast_model = AstModel()
    ast_module.ast_model.name = "ExampleTestImportsAndModel"
    ast_module.ast_model.fields = [
        AstModelField("count", "int"),
        AstModelField("example", "int", 0),
    ]
    ast_module_source = render(ast_module)
    assert (
        dedent("""
        import decimal
        from datetime import datetime
        from uuid import UUID

        @dataclass
        class ExampleTestImportsAndModel:
            count: int
            example: int = 0
        """).strip()
        == ast_module_source
    )


def test_type_alias():
    ata = AstTypeAlias(name="ExampleAlias", value="list[SomeOtherClass]")
    ast_module = AstModule(ast_type_aliases=[ata])
    ast_module_source = render(ast_module)
    assert (
        dedent("""
        ExampleAlias = list[SomeOtherClass]
        """).strip()
        == ast_module_source
    )
