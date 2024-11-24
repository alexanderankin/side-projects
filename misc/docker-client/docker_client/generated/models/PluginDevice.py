from typing import *

from pydantic import BaseModel, Field


class PluginDevice(BaseModel):
    """
    None model

    """

    Name: str = Field(alias="Name")

    Description: str = Field(alias="Description")

    Settable: List[str] = Field(alias="Settable")

    Path: str = Field(alias="Path")
