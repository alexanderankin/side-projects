from typing import *

from pydantic import BaseModel, Field


class AuthConfig(BaseModel):
    """
    None model

    """

    username: Optional[str] = Field(alias="username", default=None)

    password: Optional[str] = Field(alias="password", default=None)

    email: Optional[str] = Field(alias="email", default=None)

    serveraddress: Optional[str] = Field(alias="serveraddress", default=None)
