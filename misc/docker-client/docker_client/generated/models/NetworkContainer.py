from typing import *

from pydantic import BaseModel, Field


class NetworkContainer(BaseModel):
    """
    None model

    """

    Name: Optional[str] = Field(alias="Name", default=None)

    EndpointID: Optional[str] = Field(alias="EndpointID", default=None)

    MacAddress: Optional[str] = Field(alias="MacAddress", default=None)

    IPv4Address: Optional[str] = Field(alias="IPv4Address", default=None)

    IPv6Address: Optional[str] = Field(alias="IPv6Address", default=None)
