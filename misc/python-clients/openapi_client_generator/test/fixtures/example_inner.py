from dataclasses import dataclass
from typing import Union


@dataclass
class ExampleInner:
    name: str
    description: str | None = None
    info: Union["ExampleInner.Info", None] = None

    @dataclass
    class Info:
        count: int | None = None
