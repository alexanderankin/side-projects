from typing import *

from pydantic import BaseModel, Field

from .GenericResources import GenericResources
from .ObjectVersion import ObjectVersion
from .TaskSpec import TaskSpec
from .TaskState import TaskState
from .TaskStatus import TaskStatus


class Task(BaseModel):
    """
    None model

    """

    ID: Optional[str] = Field(alias="ID", default=None)

    Version: Optional[ObjectVersion] = Field(alias="Version", default=None)

    CreatedAt: Optional[str] = Field(alias="CreatedAt", default=None)

    UpdatedAt: Optional[str] = Field(alias="UpdatedAt", default=None)

    Name: Optional[str] = Field(alias="Name", default=None)

    Labels: Optional[Dict[str, Any]] = Field(alias="Labels", default=None)

    Spec: Optional[TaskSpec] = Field(alias="Spec", default=None)

    ServiceID: Optional[str] = Field(alias="ServiceID", default=None)

    Slot: Optional[int] = Field(alias="Slot", default=None)

    NodeID: Optional[str] = Field(alias="NodeID", default=None)

    AssignedGenericResources: Optional[GenericResources] = Field(alias="AssignedGenericResources", default=None)

    Status: Optional[TaskStatus] = Field(alias="Status", default=None)

    DesiredState: Optional[TaskState] = Field(alias="DesiredState", default=None)

    JobIteration: Optional[ObjectVersion] = Field(alias="JobIteration", default=None)
