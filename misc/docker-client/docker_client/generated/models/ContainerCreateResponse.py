from typing import *

from pydantic import BaseModel, Field


class ContainerCreateResponse(BaseModel):
    """
    ContainerCreateResponse model
        OK response to ContainerCreate operation

    """

    Id: str = Field(alias="Id")

    Warnings: List[str] = Field(alias="Warnings")
