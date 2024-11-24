from typing import *

from pydantic import BaseModel, Field

from .ConfigReference import ConfigReference
from .IPAM import IPAM
from .PeerInfo import PeerInfo


class Network(BaseModel):
    """
    None model

    """

    Name: Optional[str] = Field(alias="Name", default=None)

    Id: Optional[str] = Field(alias="Id", default=None)

    Created: Optional[str] = Field(alias="Created", default=None)

    Scope: Optional[str] = Field(alias="Scope", default=None)

    Driver: Optional[str] = Field(alias="Driver", default=None)

    EnableIPv6: Optional[bool] = Field(alias="EnableIPv6", default=None)

    IPAM: Optional[IPAM] = Field(alias="IPAM", default=None)

    Internal: Optional[bool] = Field(alias="Internal", default=None)

    Attachable: Optional[bool] = Field(alias="Attachable", default=None)

    Ingress: Optional[bool] = Field(alias="Ingress", default=None)

    ConfigFrom: Optional[ConfigReference] = Field(alias="ConfigFrom", default=None)

    ConfigOnly: Optional[bool] = Field(alias="ConfigOnly", default=None)

    Containers: Optional[Dict[str, Any]] = Field(alias="Containers", default=None)

    Options: Optional[Dict[str, Any]] = Field(alias="Options", default=None)

    Labels: Optional[Dict[str, Any]] = Field(alias="Labels", default=None)

    Peers: Optional[List[Optional[PeerInfo]]] = Field(alias="Peers", default=None)
