from typing import *

from pydantic import BaseModel, Field

from .GraphDriverData import GraphDriverData
from .ImageConfig import ImageConfig


class ImageInspect(BaseModel):
    """
    None model
        Information about an image in the local image cache.


    """

    Id: Optional[str] = Field(alias="Id", default=None)

    RepoTags: Optional[List[str]] = Field(alias="RepoTags", default=None)

    RepoDigests: Optional[List[str]] = Field(alias="RepoDigests", default=None)

    Parent: Optional[str] = Field(alias="Parent", default=None)

    Comment: Optional[str] = Field(alias="Comment", default=None)

    Created: Optional[str] = Field(alias="Created", default=None)

    DockerVersion: Optional[str] = Field(alias="DockerVersion", default=None)

    Author: Optional[str] = Field(alias="Author", default=None)

    Config: Optional[ImageConfig] = Field(alias="Config", default=None)

    Architecture: Optional[str] = Field(alias="Architecture", default=None)

    Variant: Optional[str] = Field(alias="Variant", default=None)

    Os: Optional[str] = Field(alias="Os", default=None)

    OsVersion: Optional[str] = Field(alias="OsVersion", default=None)

    Size: Optional[int] = Field(alias="Size", default=None)

    VirtualSize: Optional[int] = Field(alias="VirtualSize", default=None)

    GraphDriver: Optional[GraphDriverData] = Field(alias="GraphDriver", default=None)

    RootFS: Optional[Dict[str, Any]] = Field(alias="RootFS", default=None)

    Metadata: Optional[Dict[str, Any]] = Field(alias="Metadata", default=None)
