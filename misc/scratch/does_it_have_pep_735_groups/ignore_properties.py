from dataclasses import fields
from typing import TypeVar, Type

IPT = TypeVar('IPT')


def ignore_properties(cls: Type[IPT], dict_: any) -> IPT:
    """omits extra fields like @JsonIgnoreProperties(ignoreUnknown = true)"""
    if isinstance(dict_, cls): return dict_  # noqa
    class_fields = {f.name for f in fields(cls)}
    filtered = {k: v for k, v in dict_.items() if k in class_fields}
    return cls(**filtered)
