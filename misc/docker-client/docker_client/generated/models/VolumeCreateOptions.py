from typing import *

from pydantic import BaseModel, Field

from .ClusterVolumeSpec import ClusterVolumeSpec


class VolumeCreateOptions(BaseModel):
    """
    VolumeConfig model
        Volume configuration

    """

    Name: Optional[str] = Field(alias="Name", default=None)

    Driver: Optional[str] = Field(alias="Driver", default=None)

    DriverOpts: Optional[Dict[str, Any]] = Field(alias="DriverOpts", default=None)

    Labels: Optional[Dict[str, Any]] = Field(alias="Labels", default=None)

    ClusterVolumeSpec: Optional[ClusterVolumeSpec] = Field(alias="ClusterVolumeSpec", default=None)
