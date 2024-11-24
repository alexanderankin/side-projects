from typing import *

from pydantic import BaseModel, Field


class EndpointIPAMConfig(BaseModel):
    """
    None model
        EndpointIPAMConfig represents an endpoint&#39;s IPAM configuration.


    """

    IPv4Address: Optional[str] = Field(alias="IPv4Address", default=None)

    IPv6Address: Optional[str] = Field(alias="IPv6Address", default=None)

    LinkLocalIPs: Optional[List[str]] = Field(alias="LinkLocalIPs", default=None)
