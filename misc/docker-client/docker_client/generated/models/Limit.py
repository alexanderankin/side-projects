from typing import *

from pydantic import BaseModel, Field


class Limit(BaseModel):
    """
    None model
        An object describing a limit on resources which can be requested by a task.


    """

    NanoCPUs: Optional[int] = Field(alias="NanoCPUs", default=None)

    MemoryBytes: Optional[int] = Field(alias="MemoryBytes", default=None)

    Pids: Optional[int] = Field(alias="Pids", default=None)
