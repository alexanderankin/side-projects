from typing import *

from pydantic import BaseModel, Field

from .Volume import Volume


class VolumeListResponse(BaseModel):
    """
    VolumeListResponse model
        Volume list response

    """

    Volumes: Optional[List[Optional[Volume]]] = Field(alias="Volumes", default=None)

    Warnings: Optional[List[str]] = Field(alias="Warnings", default=None)
