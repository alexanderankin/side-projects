from typing import *

from pydantic import BaseModel, Field

from .NodeState import NodeState


class NodeStatus(BaseModel):
    """
        None model
            NodeStatus represents the status of a node.

    It provides the current status of the node, as seen by the manager.


    """

    State: Optional[NodeState] = Field(alias="State", default=None)

    Message: Optional[str] = Field(alias="Message", default=None)

    Addr: Optional[str] = Field(alias="Addr", default=None)
