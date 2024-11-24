from typing import *

from pydantic import BaseModel, Field


class JoinTokens(BaseModel):
    """
    None model
        JoinTokens contains the tokens workers and managers need to join the swarm.


    """

    Worker: Optional[str] = Field(alias="Worker", default=None)

    Manager: Optional[str] = Field(alias="Manager", default=None)
