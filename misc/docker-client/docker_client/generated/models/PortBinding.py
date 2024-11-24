from typing import *

from pydantic import BaseModel, Field


class PortBinding(BaseModel):
    """
        None model
            PortBinding represents a binding between a host IP address and a host
    port.


    """

    HostIp: Optional[str] = Field(alias="HostIp", default=None)

    HostPort: Optional[str] = Field(alias="HostPort", default=None)
