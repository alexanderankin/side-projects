from abc import ABC
from typing import Any


class RequestClient(ABC):
    def exchange(self, method: str, url: str, body: dict[str, Any]) -> dict[str, Any]:
        ...

    def exchange_any(self, body: dict[str, Any]) -> str:
        ...

    def stream(self, body: dict[str, Any]) -> str:
        """todo improve return type or whatever"""
        raise NotImplemented("todo")
