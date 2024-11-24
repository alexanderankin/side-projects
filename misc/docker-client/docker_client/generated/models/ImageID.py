from typing import *

from pydantic import BaseModel, Field


class ImageID(BaseModel):
    """
    None model
        Image ID or Digest

    """

    ID: Optional[str] = Field(alias="ID", default=None)
