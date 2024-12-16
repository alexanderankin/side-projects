from typing import Any

from openapi_client_generator.ast_model_model import AstModel, AstModelField, AstModule


def generate_single_model_file(openapi_spec: dict[str, Any]) -> AstModule:
    ast_module = AstModule()

    schemas = openapi_spec.get("components", {}).get("schemas", {})
    if isinstance(schemas, dict):
        for key, value in schemas.items():
            ast_module.ast_models = ast_module.ast_models or []
            model = _generate_model(key, value, schemas, ast_module)
            if model:
                ast_module.ast_models.append(model)

    return ast_module


def _generate_model(
    key: str, value: dict[str, Any], schemas: dict[str, Any], ast_module: AstModule
):
    name = key
    description = value.get("description") or name
    properties = (
        value.get("properties") if isinstance(value.get("properties"), dict) else {}
    )
    required = (
        value.get("required") if isinstance(value.get("required"), list) else None
    )
    fields: list[AstModelField] = []

    if required is not None:
        for each_required_field in required:
            fields.append(_gen_field(each_required_field, properties))

    for each_field in list(properties):
        fields.append(_gen_field(each_field, properties))

    ast_model = AstModel(name=name, description=description, fields=fields)
    return ast_model


def _gen_field(each_required_field, properties):
    field_info = properties.pop(each_required_field)
    if "default" in field_info:
        literal_schema_default = field_info["default"]
        schema_default = (
            "None" if literal_schema_default is None else literal_schema_default
        )
    elif field_info.get("nullable"):
        schema_default = "None"
    else:
        schema_default = None

    field_type = _generate_model_type(field_info)
    field_description = field_info.get("description")

    field = AstModelField(
        name=each_required_field,
        type=field_type,
        default=schema_default,
        description=field_description,
    )
    return field


_SIMPLE_TYPES = {
    "string": "str",
    "integer": "int",
    "boolean": "bool",
}


def _generate_model_type(field_info: dict[str, Any]):
    schema_type = field_info.get("type")

    simple_type = _SIMPLE_TYPES.get(schema_type)

    if simple_type:
        return simple_type

    if "additionalProperties" in field_info:
        return "dict"

    if schema_type == "array":
        element_type = _generate_model_type(field_info.get("items"))
        return f"list[{element_type}]"

    if schema_type == "object":
        # element_type = _generate_model_type(field_info.get("items"))
        return f"dict"

    return "dict"
