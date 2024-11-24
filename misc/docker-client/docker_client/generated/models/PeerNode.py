from typing import *

from pydantic import BaseModel, Field


class PeerNode(BaseModel):
    """
    None model
        Represents a peer-node in the swarm

    """

    NodeID: Optional[str] = Field(alias="NodeID", default=None)

    Addr: Optional[str] = Field(alias="Addr", default=None)
