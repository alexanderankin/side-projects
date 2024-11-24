from typing import *

from pydantic import BaseModel, Field


class Driver(BaseModel):
    """
    None model
        Driver represents a driver (network, logging, secrets).

    """

    Name: str = Field(alias="Name")

    Options: Optional[Dict[str, Any]] = Field(alias="Options", default=None)
