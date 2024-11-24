from typing import *

from pydantic import BaseModel, Field


class NetworkingConfig(BaseModel):
    """
        None model
            NetworkingConfig represents the container&#39;s networking configuration for
    each of its interfaces.
    It is used for the networking configs specified in the `docker create`
    and `docker network connect` commands.


    """

    EndpointsConfig: Optional[Dict[str, Any]] = Field(alias="EndpointsConfig", default=None)
