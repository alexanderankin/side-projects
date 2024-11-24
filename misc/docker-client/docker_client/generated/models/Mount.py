from typing import *

from pydantic import BaseModel, Field


class Mount(BaseModel):
    """
    None model

    """

    Target: Optional[str] = Field(alias="Target", default=None)

    Source: Optional[str] = Field(alias="Source", default=None)

    Type: Optional[str] = Field(alias="Type", default=None)

    ReadOnly: Optional[bool] = Field(alias="ReadOnly", default=None)

    Consistency: Optional[str] = Field(alias="Consistency", default=None)

    BindOptions: Optional[Dict[str, Any]] = Field(alias="BindOptions", default=None)

    VolumeOptions: Optional[Dict[str, Any]] = Field(alias="VolumeOptions", default=None)

    TmpfsOptions: Optional[Dict[str, Any]] = Field(alias="TmpfsOptions", default=None)
