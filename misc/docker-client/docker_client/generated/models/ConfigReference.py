from typing import *

from pydantic import BaseModel, Field


class ConfigReference(BaseModel):
    """
        None model
            The config-only network source to provide the configuration for
    this network.


    """

    Network: Optional[str] = Field(alias="Network", default=None)
