from typing import *

from pydantic import BaseModel, Field


class EngineDescription(BaseModel):
    """
    None model
        EngineDescription provides information about an engine.

    """

    EngineVersion: Optional[str] = Field(alias="EngineVersion", default=None)

    Labels: Optional[Dict[str, Any]] = Field(alias="Labels", default=None)

    Plugins: Optional[List[Dict[str, Any]]] = Field(alias="Plugins", default=None)
