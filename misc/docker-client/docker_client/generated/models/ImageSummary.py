from typing import *

from pydantic import BaseModel, Field


class ImageSummary(BaseModel):
    """
    None model

    """

    Id: str = Field(alias="Id")

    ParentId: str = Field(alias="ParentId")

    RepoTags: List[str] = Field(alias="RepoTags")

    RepoDigests: List[str] = Field(alias="RepoDigests")

    Created: int = Field(alias="Created")

    Size: int = Field(alias="Size")

    SharedSize: int = Field(alias="SharedSize")

    VirtualSize: Optional[int] = Field(alias="VirtualSize", default=None)

    Labels: Dict[str, Any] = Field(alias="Labels")

    Containers: int = Field(alias="Containers")
