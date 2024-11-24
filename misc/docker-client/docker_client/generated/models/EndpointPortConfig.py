from typing import *

from pydantic import BaseModel, Field


class EndpointPortConfig(BaseModel):
    """
    None model

    """

    Name: Optional[str] = Field(alias="Name", default=None)

    Protocol: Optional[str] = Field(alias="Protocol", default=None)

    TargetPort: Optional[int] = Field(alias="TargetPort", default=None)

    PublishedPort: Optional[int] = Field(alias="PublishedPort", default=None)

    PublishMode: Optional[str] = Field(alias="PublishMode", default=None)
