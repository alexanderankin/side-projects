from typing import *

from pydantic import BaseModel, Field

from .ClusterInfo import ClusterInfo
from .LocalNodeState import LocalNodeState
from .PeerNode import PeerNode


class SwarmInfo(BaseModel):
    """
    None model
        Represents generic information about swarm.


    """

    NodeID: Optional[str] = Field(alias="NodeID", default=None)

    NodeAddr: Optional[str] = Field(alias="NodeAddr", default=None)

    LocalNodeState: Optional[LocalNodeState] = Field(alias="LocalNodeState", default=None)

    ControlAvailable: Optional[bool] = Field(alias="ControlAvailable", default=None)

    Error: Optional[str] = Field(alias="Error", default=None)

    RemoteManagers: Optional[List[Optional[PeerNode]]] = Field(alias="RemoteManagers", default=None)

    Nodes: Optional[int] = Field(alias="Nodes", default=None)

    Managers: Optional[int] = Field(alias="Managers", default=None)

    Cluster: Optional[ClusterInfo] = Field(alias="Cluster", default=None)
