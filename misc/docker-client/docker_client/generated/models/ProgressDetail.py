from typing import *

from pydantic import BaseModel, Field


class ProgressDetail(BaseModel):
    """
    None model

    """

    current: Optional[int] = Field(alias="current", default=None)

    total: Optional[int] = Field(alias="total", default=None)
