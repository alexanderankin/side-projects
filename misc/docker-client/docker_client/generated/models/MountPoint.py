from typing import *

from pydantic import BaseModel, Field


class MountPoint(BaseModel):
    """
        None model
            MountPoint represents a mount point configuration inside the container.
    This is used for reporting the mountpoints in use by a container.


    """

    Type: Optional[str] = Field(alias="Type", default=None)

    Name: Optional[str] = Field(alias="Name", default=None)

    Source: Optional[str] = Field(alias="Source", default=None)

    Destination: Optional[str] = Field(alias="Destination", default=None)

    Driver: Optional[str] = Field(alias="Driver", default=None)

    Mode: Optional[str] = Field(alias="Mode", default=None)

    RW: Optional[bool] = Field(alias="RW", default=None)

    Propagation: Optional[str] = Field(alias="Propagation", default=None)
