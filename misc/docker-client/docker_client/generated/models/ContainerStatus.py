from typing import *

from pydantic import BaseModel, Field


class ContainerStatus(BaseModel):
    """
    None model
        represents the status of a container.

    """

    ContainerID: Optional[str] = Field(alias="ContainerID", default=None)

    PID: Optional[int] = Field(alias="PID", default=None)

    ExitCode: Optional[int] = Field(alias="ExitCode", default=None)
