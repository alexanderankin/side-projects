from typing import *

from pydantic import BaseModel, Field

from .ErrorDetail import ErrorDetail
from .ProgressDetail import ProgressDetail


class CreateImageInfo(BaseModel):
    """
    None model

    """

    id: Optional[str] = Field(alias="id", default=None)

    error: Optional[str] = Field(alias="error", default=None)

    errorDetail: Optional[ErrorDetail] = Field(alias="errorDetail", default=None)

    status: Optional[str] = Field(alias="status", default=None)

    progress: Optional[str] = Field(alias="progress", default=None)

    progressDetail: Optional[ProgressDetail] = Field(alias="progressDetail", default=None)
