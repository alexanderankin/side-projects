from typing import *

from pydantic import BaseModel, Field


class DeviceRequest(BaseModel):
    """
    None model
        A request for devices to be sent to device drivers

    """

    Driver: Optional[str] = Field(alias="Driver", default=None)

    Count: Optional[int] = Field(alias="Count", default=None)

    DeviceIDs: Optional[List[str]] = Field(alias="DeviceIDs", default=None)

    Capabilities: Optional[List[List[str]]] = Field(alias="Capabilities", default=None)

    Options: Optional[Dict[str, Any]] = Field(alias="Options", default=None)
