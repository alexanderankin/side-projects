from typing import *

from pydantic import BaseModel, Field

from .Reachability import Reachability


class ManagerStatus(BaseModel):
    """
        None model
            ManagerStatus represents the status of a manager.

    It provides the current status of a node&#39;s manager component, if the node
    is a manager.


    """

    Leader: Optional[bool] = Field(alias="Leader", default=None)

    Reachability: Optional[Reachability] = Field(alias="Reachability", default=None)

    Addr: Optional[str] = Field(alias="Addr", default=None)
