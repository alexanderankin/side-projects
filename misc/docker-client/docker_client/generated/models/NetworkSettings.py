from typing import *

from pydantic import BaseModel, Field

from .Address import Address
from .PortMap import PortMap


class NetworkSettings(BaseModel):
    """
    None model
        NetworkSettings exposes the network settings in the API

    """

    Bridge: Optional[str] = Field(alias="Bridge", default=None)

    SandboxID: Optional[str] = Field(alias="SandboxID", default=None)

    HairpinMode: Optional[bool] = Field(alias="HairpinMode", default=None)

    LinkLocalIPv6Address: Optional[str] = Field(alias="LinkLocalIPv6Address", default=None)

    LinkLocalIPv6PrefixLen: Optional[int] = Field(alias="LinkLocalIPv6PrefixLen", default=None)

    Ports: Optional[PortMap] = Field(alias="Ports", default=None)

    SandboxKey: Optional[str] = Field(alias="SandboxKey", default=None)

    SecondaryIPAddresses: Optional[List[Optional[Address]]] = Field(alias="SecondaryIPAddresses", default=None)

    SecondaryIPv6Addresses: Optional[List[Optional[Address]]] = Field(alias="SecondaryIPv6Addresses", default=None)

    EndpointID: Optional[str] = Field(alias="EndpointID", default=None)

    Gateway: Optional[str] = Field(alias="Gateway", default=None)

    GlobalIPv6Address: Optional[str] = Field(alias="GlobalIPv6Address", default=None)

    GlobalIPv6PrefixLen: Optional[int] = Field(alias="GlobalIPv6PrefixLen", default=None)

    IPAddress: Optional[str] = Field(alias="IPAddress", default=None)

    IPPrefixLen: Optional[int] = Field(alias="IPPrefixLen", default=None)

    IPv6Gateway: Optional[str] = Field(alias="IPv6Gateway", default=None)

    MacAddress: Optional[str] = Field(alias="MacAddress", default=None)

    Networks: Optional[Dict[str, Any]] = Field(alias="Networks", default=None)
