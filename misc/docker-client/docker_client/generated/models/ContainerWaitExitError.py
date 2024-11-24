from typing import *

from pydantic import BaseModel, Field


class ContainerWaitExitError(BaseModel):
    """
    None model
        container waiting error, if any

    """

    Message: Optional[str] = Field(alias="Message", default=None)
