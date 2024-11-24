from typing import *

from pydantic import BaseModel, Field


class PluginInterfaceType(BaseModel):
    """
    None model

    """

    Prefix: str = Field(alias="Prefix")

    Capability: str = Field(alias="Capability")

    Version: str = Field(alias="Version")
