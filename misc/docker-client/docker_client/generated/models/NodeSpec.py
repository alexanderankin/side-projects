from typing import *

from pydantic import BaseModel, Field


class NodeSpec(BaseModel):
    """
    None model

    """

    Name: Optional[str] = Field(alias="Name", default=None)

    Labels: Optional[Dict[str, Any]] = Field(alias="Labels", default=None)

    Role: Optional[str] = Field(alias="Role", default=None)

    Availability: Optional[str] = Field(alias="Availability", default=None)
