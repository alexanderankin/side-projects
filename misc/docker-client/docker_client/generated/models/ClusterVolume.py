from typing import *

from pydantic import BaseModel, Field

from .ClusterVolumeSpec import ClusterVolumeSpec
from .ObjectVersion import ObjectVersion


class ClusterVolume(BaseModel):
    """
        None model
            Options and information specific to, and only present on, Swarm CSI
    cluster volumes.


    """

    ID: Optional[str] = Field(alias="ID", default=None)

    Version: Optional[ObjectVersion] = Field(alias="Version", default=None)

    CreatedAt: Optional[str] = Field(alias="CreatedAt", default=None)

    UpdatedAt: Optional[str] = Field(alias="UpdatedAt", default=None)

    Spec: Optional[ClusterVolumeSpec] = Field(alias="Spec", default=None)

    Info: Optional[Dict[str, Any]] = Field(alias="Info", default=None)

    PublishStatus: Optional[List[Dict[str, Any]]] = Field(alias="PublishStatus", default=None)
