from typing import *

from pydantic import BaseModel, Field

from .EndpointPortConfig import EndpointPortConfig


class EndpointSpec(BaseModel):
    """
    None model
        Properties that can be configured to access and load balance a service.

    """

    Mode: Optional[str] = Field(alias="Mode", default=None)

    Ports: Optional[List[Optional[EndpointPortConfig]]] = Field(alias="Ports", default=None)
