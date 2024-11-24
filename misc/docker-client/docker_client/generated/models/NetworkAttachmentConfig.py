from typing import *

from pydantic import BaseModel, Field


class NetworkAttachmentConfig(BaseModel):
    """
    None model
        Specifies how a service should be attached to a particular network.


    """

    Target: Optional[str] = Field(alias="Target", default=None)

    Aliases: Optional[List[str]] = Field(alias="Aliases", default=None)

    DriverOpts: Optional[Dict[str, Any]] = Field(alias="DriverOpts", default=None)
