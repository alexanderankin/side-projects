from typing import *

from pydantic import BaseModel, Field


class RestartPolicy(BaseModel):
    """
        None model
            The behavior to apply when the container exits. The default is not to
    restart.

    An ever increasing delay (double the previous delay, starting at 100ms) is
    added before each restart to prevent flooding the server.


    """

    Name: Optional[str] = Field(alias="Name", default=None)

    MaximumRetryCount: Optional[int] = Field(alias="MaximumRetryCount", default=None)
