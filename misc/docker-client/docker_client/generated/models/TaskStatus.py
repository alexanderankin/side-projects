from typing import *

from pydantic import BaseModel, Field

from .ContainerStatus import ContainerStatus
from .PortStatus import PortStatus
from .TaskState import TaskState


class TaskStatus(BaseModel):
    """
    None model
        represents the status of a task.

    """

    Timestamp: Optional[str] = Field(alias="Timestamp", default=None)

    State: Optional[TaskState] = Field(alias="State", default=None)

    Message: Optional[str] = Field(alias="Message", default=None)

    Err: Optional[str] = Field(alias="Err", default=None)

    ContainerStatus: Optional[ContainerStatus] = Field(alias="ContainerStatus", default=None)

    PortStatus: Optional[PortStatus] = Field(alias="PortStatus", default=None)
