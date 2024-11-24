from typing import *

from pydantic import BaseModel, Field


class Plugin(BaseModel):
    """
    None model
        A plugin for the Engine API

    """

    Id: Optional[str] = Field(alias="Id", default=None)

    Name: str = Field(alias="Name")

    Enabled: bool = Field(alias="Enabled")

    Settings: Dict[str, Any] = Field(alias="Settings")

    PluginReference: Optional[str] = Field(alias="PluginReference", default=None)

    Config: Dict[str, Any] = Field(alias="Config")
