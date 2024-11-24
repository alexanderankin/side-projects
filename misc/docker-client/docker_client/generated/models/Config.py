from typing import *

from pydantic import BaseModel, Field

from .ConfigSpec import ConfigSpec
from .ObjectVersion import ObjectVersion


class Config(BaseModel):
    """
    None model

    """

    ID: Optional[str] = Field(alias="ID", default=None)

    Version: Optional[ObjectVersion] = Field(alias="Version", default=None)

    CreatedAt: Optional[str] = Field(alias="CreatedAt", default=None)

    UpdatedAt: Optional[str] = Field(alias="UpdatedAt", default=None)

    Spec: Optional[ConfigSpec] = Field(alias="Spec", default=None)
