from dataclasses import dataclass
import os

@dataclass
class Example:
    name: str
    description: str | None = None
    os_path: os.PathLike | None = None
