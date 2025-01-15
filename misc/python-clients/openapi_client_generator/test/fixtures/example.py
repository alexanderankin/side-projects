from dataclasses import dataclass
import os

ExampleStr = str

@dataclass
class Example:
    name: ExampleStr
    description: str | None = None
    os_path: os.PathLike | None = None

    def path_as_str(self):
        """hello
        """
        return str(self.os_path)
