from typing import *

from pydantic import BaseModel, Field


class ImageDeleteResponseItem(BaseModel):
    """
    None model

    """

    Untagged: Optional[str] = Field(alias="Untagged", default=None)

    Deleted: Optional[str] = Field(alias="Deleted", default=None)
