from typing import *

from pydantic import BaseModel, Field

from .ContainerWaitExitError import ContainerWaitExitError


class ContainerWaitResponse(BaseModel):
    """
    ContainerWaitResponse model
        OK response to ContainerWait operation

    """

    StatusCode: int = Field(alias="StatusCode")

    Error: Optional[ContainerWaitExitError] = Field(alias="Error", default=None)
