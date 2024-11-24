from typing import *

from pydantic import BaseModel, Field

from .ChangeType import ChangeType


class FilesystemChange(BaseModel):
    """
    None model
        Change in the container&#39;s filesystem.


    """

    Path: str = Field(alias="Path")

    Kind: ChangeType = Field(alias="Kind")
