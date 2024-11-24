from typing import *

from pydantic import BaseModel, Field

from .ClusterVolume import ClusterVolume


class Volume(BaseModel):
    """
    None model

    """

    Name: str = Field(alias="Name")

    Driver: str = Field(alias="Driver")

    Mountpoint: str = Field(alias="Mountpoint")

    CreatedAt: Optional[str] = Field(alias="CreatedAt", default=None)

    Status: Optional[Dict[str, Any]] = Field(alias="Status", default=None)

    Labels: Dict[str, Any] = Field(alias="Labels")

    Scope: str = Field(alias="Scope")

    ClusterVolume: Optional[ClusterVolume] = Field(alias="ClusterVolume", default=None)

    Options: Dict[str, Any] = Field(alias="Options")

    UsageData: Optional[Dict[str, Any]] = Field(alias="UsageData", default=None)
