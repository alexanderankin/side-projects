from typing import *

from pydantic import BaseModel, Field

from .Driver import Driver


class ConfigSpec(BaseModel):
    """
    None model

    """

    Name: Optional[str] = Field(alias="Name", default=None)

    Labels: Optional[Dict[str, Any]] = Field(alias="Labels", default=None)

    Data: Optional[str] = Field(alias="Data", default=None)

    Templating: Optional[Driver] = Field(alias="Templating", default=None)
