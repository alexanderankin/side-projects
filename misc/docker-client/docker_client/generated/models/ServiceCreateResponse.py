from typing import *

from pydantic import BaseModel, Field


class ServiceCreateResponse(BaseModel):
    """
        None model
            contains the information returned to a client on the
    creation of a new service.


    """

    ID: Optional[str] = Field(alias="ID", default=None)

    Warnings: Optional[List[str]] = Field(alias="Warnings", default=None)
