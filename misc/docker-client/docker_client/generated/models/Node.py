from typing import *

from pydantic import BaseModel, Field

from .ManagerStatus import ManagerStatus
from .NodeDescription import NodeDescription
from .NodeSpec import NodeSpec
from .NodeStatus import NodeStatus
from .ObjectVersion import ObjectVersion


class Node(BaseModel):
    """
    None model

    """

    ID: Optional[str] = Field(alias="ID", default=None)

    Version: Optional[ObjectVersion] = Field(alias="Version", default=None)

    CreatedAt: Optional[str] = Field(alias="CreatedAt", default=None)

    UpdatedAt: Optional[str] = Field(alias="UpdatedAt", default=None)

    Spec: Optional[NodeSpec] = Field(alias="Spec", default=None)

    Description: Optional[NodeDescription] = Field(alias="Description", default=None)

    Status: Optional[NodeStatus] = Field(alias="Status", default=None)

    ManagerStatus: Optional[ManagerStatus] = Field(alias="ManagerStatus", default=None)
