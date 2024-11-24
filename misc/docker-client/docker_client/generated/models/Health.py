from typing import *

from pydantic import BaseModel, Field

from .HealthcheckResult import HealthcheckResult


class Health(BaseModel):
    """
    None model
        Health stores information about the container&#39;s healthcheck results.


    """

    Status: Optional[str] = Field(alias="Status", default=None)

    FailingStreak: Optional[int] = Field(alias="FailingStreak", default=None)

    Log: Optional[List[Optional[HealthcheckResult]]] = Field(alias="Log", default=None)
