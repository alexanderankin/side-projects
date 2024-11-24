from typing import *

from pydantic import BaseModel, Field

from .ErrorDetail import ErrorDetail
from .ImageID import ImageID
from .ProgressDetail import ProgressDetail


class BuildInfo(BaseModel):
    """
    None model

    """

    id: Optional[str] = Field(alias="id", default=None)

    stream: Optional[str] = Field(alias="stream", default=None)

    error: Optional[str] = Field(alias="error", default=None)

    errorDetail: Optional[ErrorDetail] = Field(alias="errorDetail", default=None)

    status: Optional[str] = Field(alias="status", default=None)

    progress: Optional[str] = Field(alias="progress", default=None)

    progressDetail: Optional[ProgressDetail] = Field(alias="progressDetail", default=None)

    aux: Optional[ImageID] = Field(alias="aux", default=None)
