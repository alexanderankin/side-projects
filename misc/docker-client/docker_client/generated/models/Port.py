from typing import *

from pydantic import BaseModel, Field


class Port(BaseModel):
    """
    None model
        An open port on a container

    """

    IP: Optional[str] = Field(alias="IP", default=None)

    PrivatePort: int = Field(alias="PrivatePort")

    PublicPort: Optional[int] = Field(alias="PublicPort", default=None)

    Type: str = Field(alias="Type")
