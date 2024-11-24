from typing import *

from pydantic import BaseModel, Field

from .ObjectVersion import ObjectVersion
from .SwarmSpec import SwarmSpec
from .TLSInfo import TLSInfo


class ClusterInfo(BaseModel):
    """
        None model
            ClusterInfo represents information about the swarm as is returned by the
    &#34;/info&#34; endpoint. Join-tokens are not included.


    """

    ID: Optional[str] = Field(alias="ID", default=None)

    Version: Optional[ObjectVersion] = Field(alias="Version", default=None)

    CreatedAt: Optional[str] = Field(alias="CreatedAt", default=None)

    UpdatedAt: Optional[str] = Field(alias="UpdatedAt", default=None)

    Spec: Optional[SwarmSpec] = Field(alias="Spec", default=None)

    TLSInfo: Optional[TLSInfo] = Field(alias="TLSInfo", default=None)

    RootRotationInProgress: Optional[bool] = Field(alias="RootRotationInProgress", default=None)

    DataPathPort: Optional[int] = Field(alias="DataPathPort", default=None)

    DefaultAddrPool: Optional[List[str]] = Field(alias="DefaultAddrPool", default=None)

    SubnetSize: Optional[int] = Field(alias="SubnetSize", default=None)
