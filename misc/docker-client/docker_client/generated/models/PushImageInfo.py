from typing import *

from pydantic import BaseModel, Field

from .ProgressDetail import ProgressDetail


class PushImageInfo(BaseModel):
    """
    None model

    """

    error: Optional[str] = Field(alias="error", default=None)

    status: Optional[str] = Field(alias="status", default=None)

    progress: Optional[str] = Field(alias="progress", default=None)

    progressDetail: Optional[ProgressDetail] = Field(alias="progressDetail", default=None)
