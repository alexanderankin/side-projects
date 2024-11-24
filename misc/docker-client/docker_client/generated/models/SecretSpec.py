from typing import *

from pydantic import BaseModel, Field

from .Driver import Driver


class SecretSpec(BaseModel):
    """
    None model

    """

    Name: Optional[str] = Field(alias="Name", default=None)

    Labels: Optional[Dict[str, Any]] = Field(alias="Labels", default=None)

    Data: Optional[str] = Field(alias="Data", default=None)

    Driver: Optional[Driver] = Field(alias="Driver", default=None)

    Templating: Optional[Driver] = Field(alias="Templating", default=None)
