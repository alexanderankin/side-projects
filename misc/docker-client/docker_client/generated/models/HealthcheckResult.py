from typing import *

from pydantic import BaseModel, Field


class HealthcheckResult(BaseModel):
    """
    None model
        HealthcheckResult stores information about a single run of a healthcheck probe


    """

    Start: Optional[str] = Field(alias="Start", default=None)

    End: Optional[str] = Field(alias="End", default=None)

    ExitCode: Optional[int] = Field(alias="ExitCode", default=None)

    Output: Optional[str] = Field(alias="Output", default=None)
