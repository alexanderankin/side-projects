from typing import *

from pydantic import BaseModel, Field


class ServiceUpdateResponse(BaseModel):
    """
    None model

    """

    Warnings: Optional[List[str]] = Field(alias="Warnings", default=None)
