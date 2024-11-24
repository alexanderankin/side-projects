from typing import *

from pydantic import BaseModel, Field


class ClusterVolumeSpec(BaseModel):
    """
    None model
        Cluster-specific options used to create the volume.


    """

    Group: Optional[str] = Field(alias="Group", default=None)

    AccessMode: Optional[Dict[str, Any]] = Field(alias="AccessMode", default=None)
