from typing import *

from pydantic import BaseModel, Field


class Platform(BaseModel):
    """
    None model
        Platform represents the platform (Arch/OS).


    """

    Architecture: Optional[str] = Field(alias="Architecture", default=None)

    OS: Optional[str] = Field(alias="OS", default=None)
