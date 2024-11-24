from typing import *

from pydantic import BaseModel, Field


class IdResponse(BaseModel):
    """
    None model
        Response to an API call that returns just an Id

    """

    Id: str = Field(alias="Id")
