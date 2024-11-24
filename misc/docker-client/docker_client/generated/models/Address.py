from typing import *

from pydantic import BaseModel, Field


class Address(BaseModel):
    """
    None model
        Address represents an IPv4 or IPv6 IP address.

    """

    Addr: Optional[str] = Field(alias="Addr", default=None)

    PrefixLen: Optional[int] = Field(alias="PrefixLen", default=None)
