from typing import *

from pydantic import BaseModel, Field


class PluginEnv(BaseModel):
    """
    None model

    """

    Name: str = Field(alias="Name")

    Description: str = Field(alias="Description")

    Settable: List[str] = Field(alias="Settable")

    Value: str = Field(alias="Value")
