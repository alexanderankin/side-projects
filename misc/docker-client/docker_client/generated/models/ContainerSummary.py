from typing import *

from pydantic import BaseModel, Field

from .MountPoint import MountPoint
from .Port import Port


class ContainerSummary(BaseModel):
    """
    None model

    """

    Id: Optional[str] = Field(alias="Id", default=None)

    Names: Optional[List[str]] = Field(alias="Names", default=None)

    Image: Optional[str] = Field(alias="Image", default=None)

    ImageID: Optional[str] = Field(alias="ImageID", default=None)

    Command: Optional[str] = Field(alias="Command", default=None)

    Created: Optional[int] = Field(alias="Created", default=None)

    Ports: Optional[List[Optional[Port]]] = Field(alias="Ports", default=None)

    SizeRw: Optional[int] = Field(alias="SizeRw", default=None)

    SizeRootFs: Optional[int] = Field(alias="SizeRootFs", default=None)

    Labels: Optional[Dict[str, Any]] = Field(alias="Labels", default=None)

    State: Optional[str] = Field(alias="State", default=None)

    Status: Optional[str] = Field(alias="Status", default=None)

    HostConfig: Optional[Dict[str, Any]] = Field(alias="HostConfig", default=None)

    NetworkSettings: Optional[Dict[str, Any]] = Field(alias="NetworkSettings", default=None)

    Mounts: Optional[List[Optional[MountPoint]]] = Field(alias="Mounts", default=None)
