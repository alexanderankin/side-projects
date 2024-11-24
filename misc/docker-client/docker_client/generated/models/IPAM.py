from typing import *

from pydantic import BaseModel, Field

from .IPAMConfig import IPAMConfig


class IPAM(BaseModel):
    """
    None model

    """

    Driver: Optional[str] = Field(alias="Driver", default=None)

    Config: Optional[List[Optional[IPAMConfig]]] = Field(alias="Config", default=None)

    Options: Optional[Dict[str, Any]] = Field(alias="Options", default=None)
