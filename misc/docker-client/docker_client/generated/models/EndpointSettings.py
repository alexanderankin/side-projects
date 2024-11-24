from typing import *

from pydantic import BaseModel, Field

from .EndpointIPAMConfig import EndpointIPAMConfig


class EndpointSettings(BaseModel):
    """
    None model
        Configuration for a network endpoint.

    """

    IPAMConfig: Optional[EndpointIPAMConfig] = Field(alias="IPAMConfig", default=None)

    Links: Optional[List[str]] = Field(alias="Links", default=None)

    MacAddress: Optional[str] = Field(alias="MacAddress", default=None)

    Aliases: Optional[List[str]] = Field(alias="Aliases", default=None)

    NetworkID: Optional[str] = Field(alias="NetworkID", default=None)

    EndpointID: Optional[str] = Field(alias="EndpointID", default=None)

    Gateway: Optional[str] = Field(alias="Gateway", default=None)

    IPAddress: Optional[str] = Field(alias="IPAddress", default=None)

    IPPrefixLen: Optional[int] = Field(alias="IPPrefixLen", default=None)

    IPv6Gateway: Optional[str] = Field(alias="IPv6Gateway", default=None)

    GlobalIPv6Address: Optional[str] = Field(alias="GlobalIPv6Address", default=None)

    GlobalIPv6PrefixLen: Optional[int] = Field(alias="GlobalIPv6PrefixLen", default=None)

    DriverOpts: Optional[Dict[str, Any]] = Field(alias="DriverOpts", default=None)

    DNSNames: Optional[List[str]] = Field(alias="DNSNames", default=None)
