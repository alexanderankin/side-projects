from typing import *

from pydantic import BaseModel, Field


class HealthConfig(BaseModel):
    """
    None model
        A test to perform to check that the container is healthy.

    """

    Test: Optional[List[str]] = Field(alias="Test", default=None)

    Interval: Optional[int] = Field(alias="Interval", default=None)

    Timeout: Optional[int] = Field(alias="Timeout", default=None)

    Retries: Optional[int] = Field(alias="Retries", default=None)

    StartPeriod: Optional[int] = Field(alias="StartPeriod", default=None)

    StartInterval: Optional[int] = Field(alias="StartInterval", default=None)
