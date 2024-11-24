from typing import *

from pydantic import BaseModel, Field


class PeerInfo(BaseModel):
    """
    None model
        PeerInfo represents one peer of an overlay network.


    """

    Name: Optional[str] = Field(alias="Name", default=None)

    IP: Optional[str] = Field(alias="IP", default=None)
