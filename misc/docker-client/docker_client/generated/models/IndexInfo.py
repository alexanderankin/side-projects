from typing import *

from pydantic import BaseModel, Field


class IndexInfo(BaseModel):
    """
    None model
        IndexInfo contains information about a registry.

    """

    Name: Optional[str] = Field(alias="Name", default=None)

    Mirrors: Optional[List[str]] = Field(alias="Mirrors", default=None)

    Secure: Optional[bool] = Field(alias="Secure", default=None)

    Official: Optional[bool] = Field(alias="Official", default=None)
