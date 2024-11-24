from typing import *

from pydantic import BaseModel, Field


class DeviceMapping(BaseModel):
    """
    None model
        A device mapping between the host and container

    """

    PathOnHost: Optional[str] = Field(alias="PathOnHost", default=None)

    PathInContainer: Optional[str] = Field(alias="PathInContainer", default=None)

    CgroupPermissions: Optional[str] = Field(alias="CgroupPermissions", default=None)
