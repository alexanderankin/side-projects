from typing import *

from pydantic import BaseModel, Field


class ThrottleDevice(BaseModel):
    """
    None model

    """

    Path: Optional[str] = Field(alias="Path", default=None)

    Rate: Optional[int] = Field(alias="Rate", default=None)
