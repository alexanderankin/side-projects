from typing import *

from pydantic import BaseModel, Field


class IPAMConfig(BaseModel):
    """
    None model

    """

    Subnet: Optional[str] = Field(alias="Subnet", default=None)

    IPRange: Optional[str] = Field(alias="IPRange", default=None)

    Gateway: Optional[str] = Field(alias="Gateway", default=None)

    AuxiliaryAddresses: Optional[Dict[str, Any]] = Field(alias="AuxiliaryAddresses", default=None)
