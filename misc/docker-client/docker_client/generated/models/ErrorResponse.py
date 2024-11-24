from typing import *

from pydantic import BaseModel, Field


class ErrorResponse(BaseModel):
    """
    None model
        Represents an error.

    """

    message: str = Field(alias="message")
