from typing import *

from pydantic import BaseModel, Field

from .EndpointPortConfig import EndpointPortConfig


class PortStatus(BaseModel):
    """
    None model
        represents the port status of a task&#39;s host ports whose service has published host ports

    """

    Ports: Optional[List[Optional[EndpointPortConfig]]] = Field(alias="Ports", default=None)
