from typing import *

from pydantic import BaseModel, Field


class BuildCache(BaseModel):
    """
    None model
        BuildCache contains information about a build cache record.


    """

    ID: Optional[str] = Field(alias="ID", default=None)

    Parent: Optional[str] = Field(alias="Parent", default=None)

    Parents: Optional[List[str]] = Field(alias="Parents", default=None)

    Type: Optional[str] = Field(alias="Type", default=None)

    Description: Optional[str] = Field(alias="Description", default=None)

    InUse: Optional[bool] = Field(alias="InUse", default=None)

    Shared: Optional[bool] = Field(alias="Shared", default=None)

    Size: Optional[int] = Field(alias="Size", default=None)

    CreatedAt: Optional[str] = Field(alias="CreatedAt", default=None)

    LastUsedAt: Optional[str] = Field(alias="LastUsedAt", default=None)

    UsageCount: Optional[int] = Field(alias="UsageCount", default=None)
