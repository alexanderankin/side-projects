from dataclasses import dataclass, field
from textwrap import indent
from typing import Any, Union

blank_positions_dict = {
    "lineno": None,
    "col_offset": None,
    "end_lineno": None,
    "end_col_offset": None,
}

PlainImport = str
FromImport = tuple[str, list[str]]


@dataclass
class AstModule:
    plain_imports: list[PlainImport] | None = None
    """import a module by path, e.g. 'import os'

    each import goes on its own line,
    to support practice of importing modules separately
    """

    from_import_imports: list[FromImport] | None = None
    """import local variables from a module, e.g. 'from datetime import datetime'

    each 'import from' may contain multiple symbols to import,
    to support practice of grouping 'import from' statements.
    """
    ast_type_aliases: list["AstTypeAlias"] = field(default_factory=list)

    ast_model: Union["AstModel", None] = None
    ast_models: list["AstModel"] = field(default_factory=list)

    def as_ast_dict(self):
        body: list[dict[str, Any]] = []
        result = {"type": "Module", "fields": {"body": body, "type_ignores": []}}

        # handle plain imports
        body.extend(
            [
                {
                    "type": "Import",
                    "fields": {
                        "names": [
                            {
                                "type": "alias",
                                "fields": {
                                    "name": each_plain_import,
                                    "asname": None,
                                    **blank_positions_dict,
                                },
                            },
                        ],
                        **blank_positions_dict,
                    },
                }
                for each_plain_import in (self.plain_imports or [])
            ]
        )

        # handle 'from import' imports
        body.extend(
            [
                {
                    "type": "ImportFrom",
                    "fields": {
                        "module": each_import_module,
                        "names": [
                            {
                                "type": "alias",
                                "fields": {
                                    "name": each_import_name,
                                    "asname": None,
                                    **blank_positions_dict,
                                },
                            }
                            for each_import_name in each_import_names
                        ],
                        "level": 0,
                        **blank_positions_dict,
                    },
                }
                for each_import_module, each_import_names in (
                    self.from_import_imports or []
                )
            ]
        )

        # handle ast_type_aliases
        body.extend(a.as_dict() for a in self.ast_type_aliases)

        if self.ast_model is not None:
            self._ast_model(body, self.ast_model)

        if self.ast_models:
            for each in self.ast_models:
                self._ast_model(body, each)

        return result

    @staticmethod
    def _doc_str(comment: str):
        return {
            "type": "Expr",
            "fields": {
                "value": {
                    "type": "Constant",
                    "fields": {
                        "value": comment,
                        "kind": None,
                        **blank_positions_dict,
                    },
                    **blank_positions_dict,
                },
            },
        }

    def _model_fields(self, each_model_field: "AstModelField"):
        # fmt:off
        result = [{
            "type": "AnnAssign",
            "fields": {
                "target": {
                    "type": "Name",
                    "fields": {
                        "id": each_model_field.name,
                        "ctx": {"type": "Store", "fields": {}},
                        **blank_positions_dict,
                    },
                },
                "annotation": {
                    "type": "Name",
                    "fields": {
                        "id": each_model_field.type,
                        "ctx": {"type": "Store", "fields": {}},
                        **blank_positions_dict,
                    },
                },
                "value": {
                    "type": "Constant",
                    "fields": {
                        "value": each_model_field.default,
                        "kind": None,
                        **blank_positions_dict,
                    },
                }
                if each_model_field.default is not None
                else None,
                "simple": 1,
                **blank_positions_dict,
            },
        }]
        # fmt:on
        if each_model_field.description:
            result.append(self._doc_str(each_model_field.description))
        return result

    def _ast_model(self, body: list[dict[str, Any]], ast_model: "AstModel"):
        # handle the model
        # fmt:off
        doc_comment = [] if not ast_model.description else [self._doc_str(indent(ast_model.description, prefix=" " * 4).strip())]
        # fmt:on

        if ast_model is not None:
            body.append(
                {
                    "type": "ClassDef",
                    "fields": {
                        "name": ast_model.name,
                        "bases": [],
                        "keywords": [],
                        "body": doc_comment
                        + [
                            each_ast_node
                            for each_model_field in ast_model.fields
                            for each_ast_node in self._model_fields(each_model_field)
                        ],
                        "decorator_list": [
                            {
                                "type": "Name",
                                "fields": {
                                    "id": "dataclass",
                                    "ctx": {"type": "Load", "fields": {}},
                                    **blank_positions_dict,
                                },
                            }
                        ],
                        **blank_positions_dict,
                    },
                    "type_ignores": [],
                }
            )


@dataclass
class AstModelField:
    name: str
    type: str
    default: Any | None = None
    description: str | None = None


@dataclass
class AstModel:
    name: str | None = None
    description: str | None = None
    fields: list[AstModelField] = field(default_factory=list)


@dataclass
class AstTypeAlias:
    name: str | None = None
    value: str | None = None
    type_comment: str | None = None

    def as_dict(self) -> dict[str, Any]:
        return {
            "type": "Assign",
            "fields": {
                "targets": [
                    {
                        "type": "Name",
                        "fields": {
                            "id": self.name,
                            "ctx": {"fields": {}, "type": "Load"},
                            **blank_positions_dict,
                        },
                    }
                ],
                "value": {
                    "type": "Name",
                    "fields": {
                        "id": self.value,
                        "ctx": {"fields": {}, "type": "Load"},
                        **blank_positions_dict,
                    },
                },
                "type_comment": None,
                **blank_positions_dict,
            },
        }
