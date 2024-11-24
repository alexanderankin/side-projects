from typing import *

from pydantic import BaseModel, Field


class ProcessConfig(BaseModel):
    """
    None model

    """

    privileged: Optional[bool] = Field(alias="privileged", default=None)

    user: Optional[str] = Field(alias="user", default=None)

    tty: Optional[bool] = Field(alias="tty", default=None)

    entrypoint: Optional[str] = Field(alias="entrypoint", default=None)

    arguments: Optional[List[str]] = Field(alias="arguments", default=None)
