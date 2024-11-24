from typing import *

from pydantic import BaseModel, Field


class GraphDriverData(BaseModel):
    """
        None model
            Information about the storage driver used to store the container&#39;s and
    image&#39;s filesystem.


    """

    Name: str = Field(alias="Name")

    Data: Dict[str, Any] = Field(alias="Data")
