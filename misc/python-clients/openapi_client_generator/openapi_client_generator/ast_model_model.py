from dataclasses import dataclass
from typing import Union

blank_positions_dict = {
    "lineno": None,
    "col_offset": None,
    "end_lineno": None,
    "end_col_offset": None,
}


@dataclass
class AstModule:
    plain_imports: list[str] | None = None
    """import a module by path, e.g. 'import os'

    each import goes on its own line,
    to support practice of importing modules separately
    """

    from_import_imports: list[tuple[str, list[str]]] | None = None
    """import local variables from a module, e.g. 'from datetime import datetime'

    each 'import from' may contain multiple symbols to import,
    to support practice of grouping 'import from' statements.
    """

    ast_model: Union["AstModel", None] = None

    def as_ast_dict(self):
        body = []
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

        # handle the model
        if self.ast_model is not None:
            body.append(
                {
                    "type": "ClassDef",
                    "fields": {
                        "name": self.ast_model.name,
                        "bases": [],
                        "keywords": [],
                        "body": [
                            {
                                "type": "AnnAssign",
                                "fields": {
                                    "target": {
                                        "type": "Name",
                                        "fields": {
                                            "id": each_model_field[0],
                                            "ctx": {"type": "Store", "fields": {}},
                                            **blank_positions_dict,
                                        },
                                    },
                                    "annotation": {
                                        "type": "Name",
                                        "fields": {
                                            "id": each_model_field[1],
                                            "ctx": {"type": "Store", "fields": {}},
                                            **blank_positions_dict,
                                        },
                                    },
                                    "value": {
                                        "type": "Constant",
                                        "fields": {
                                            "value": each_model_field[2],
                                            "kind": None,
                                            **blank_positions_dict,
                                        },
                                    }
                                    if len(each_model_field) > 2
                                    else None,
                                    "simple": 1,
                                    **blank_positions_dict,
                                },
                            }
                            for each_model_field in self.ast_model.fields
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

        return result


AstModelFieldWithoutDefault = tuple[str, str]
AstModelFieldWithDefault = tuple[str, str, str]
AstModelField = AstModelFieldWithDefault | AstModelFieldWithoutDefault


@dataclass
class AstModel:
    name: str | None = None
    fields: list[AstModelField] | None = None
