from dataclasses import dataclass
from typing import Union


@dataclass
class ExampleDocs:
    """Example

    this one is an example"""
    name: str
    """name of the example"""
    description: str | None = None
    """optional description"""
    info: Union["ExampleDocs.Info", None] = None
    """info about the example\n    \n    if given, indicates information about the example\n"""

    @dataclass
    class Info:
        """info

        the info"""
        count: int | None = None
        """count"""
