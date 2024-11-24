from typing import *

from pydantic import BaseModel, Field


class PluginMount(BaseModel):
    """
    None model

    """

    Name: str = Field(alias="Name")

    Description: str = Field(alias="Description")

    Settable: List[str] = Field(alias="Settable")

    Source: str = Field(alias="Source")

    Destination: str = Field(alias="Destination")

    Type: str = Field(alias="Type")

    Options: List[str] = Field(alias="Options")
